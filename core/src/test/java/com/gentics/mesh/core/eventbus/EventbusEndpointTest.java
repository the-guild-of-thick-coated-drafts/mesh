
package com.gentics.mesh.core.eventbus;

import static com.gentics.mesh.MeshEvent.NODE_CREATED;
import static com.gentics.mesh.MeshEvent.NODE_DELETED;
import static com.gentics.mesh.MeshEvent.NODE_UPDATED;
import static com.gentics.mesh.MeshEvent.MESH_MIGRATION;
import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.TestDataProvider.PROJECT_NAME;
import static com.gentics.mesh.test.TestSize.FULL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gentics.mesh.MeshEvent;
import com.gentics.mesh.assertj.MeshAssertions;
import com.gentics.mesh.rest.client.MeshRestClientUtil;
import com.gentics.mesh.rest.client.MeshWebsocket;
import com.gentics.mesh.util.RxUtil;
import io.reactivex.Completable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gentics.mesh.FieldUtil;
import com.gentics.mesh.Mesh;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.node.NodeUpdateRequest;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
@MeshTestSetting(useElasticsearch = false, testSize = FULL, startServer = true)
public class EventbusEndpointTest extends AbstractMeshTest {

	private MeshWebsocket ws;

	@Before
	public void setupEventbus() throws Exception {
		ws = client().eventbus();
		// Wait for initial connection
		ws.connections().blockingFirst();
	}

	@After
	public void closeEventBus() {
		if (ws != null) {
			ws.close();
		}
	}

	@Test(timeout = 4_000)
	public void testExternalEventbusMessage(TestContext context) throws Exception {

		Async async = context.async();
		MeshEvent allowedAddress = MESH_MIGRATION;

		// Register
		ws.registerEvents(allowedAddress);

		// Handle msgs
		ws.events().firstOrError().subscribe(event -> {
			MeshAssertions.assertThat(event.getBodyAsJson().get("test").textValue()).isEqualTo("someValue");
			async.complete();
		});

		Thread.sleep(1000);
		Mesh.vertx().eventBus().send(allowedAddress.address, new JsonObject().put("test", "someValue"));
	}

	@Test(timeout = 4_000)
	public void testNodeDeleteEvent(TestContext context) throws Exception {
		Async async = context.async();

		ws.registerEvents(NODE_DELETED);

		// Handle msgs
		ws.events().firstOrError().subscribe(event -> {
			ObjectNode body = event.getBodyAsJson();
			context.assertNotNull(body.get("uuid").textValue());
			context.assertEquals("content", body.get("schemaName").textValue());
			context.assertFalse(body.has("languageTag"));
			async.complete();
		});
		call(() -> client().deleteNode(PROJECT_NAME, contentUuid()));
	}

	@Test(timeout = 4_000)
	public void testNodeDeleteLanguageEvent(TestContext context) throws Exception {
		Async async = context.async();

		ws.registerEvents(NODE_DELETED);

		// Handle msgs
		ws.events().firstOrError().subscribe(event -> {
			ObjectNode body = event.getBodyAsJson();
			context.assertNotNull(body.get("uuid").textValue());
			context.assertEquals("content", body.get("schemaName").textValue());
			context.assertEquals("en", body.get("languageTag").textValue());
			async.complete();
		});
		call(() -> client().deleteNode(PROJECT_NAME, contentUuid(), "en"));
	}

	@Test(timeout = 4_000)
	public void testNodeUpdateEvent(TestContext context) {
		Async async = context.async();

		// Register
		ws.registerEvents(NODE_UPDATED);

		// Handle msgs
		ws.events().firstOrError().subscribe(event -> {
			ObjectNode body = event.getBodyAsJson();
			assertNotNull(body.get("uuid").textValue());
			assertEquals("content", body.get("schemaName").textValue());
			async.complete();
		});

		NodeResponse response = call(() -> client().findNodeByUuid(PROJECT_NAME, contentUuid()));
		NodeUpdateRequest request = new NodeUpdateRequest();
		request.getFields().put("slug", FieldUtil.createStringField("blub"));
		request.setVersion(response.getVersion());
		request.setLanguage("en");
		call(() -> client().updateNode(PROJECT_NAME, contentUuid(), request));

		NodeResponse response2 = call(() -> client().findNodeByUuid(PROJECT_NAME, contentUuid()));
		assertNotEquals(response.getVersion(), response2.getVersion());
	}

	@Test
	public void testCustomEventHandling(TestContext context) {
		Async asyncRec = context.async();

		ws.registerEvents("custom.myEvent");

		// Handle msgs
		ws.events().firstOrError().subscribe(event -> {
			String body = event.getBodyAsString();
			assertEquals("someText", body);
			asyncRec.complete();
		});

		// Send msg
		ws.publishEvent("custom.myEvent", "someText");
	}

	@Test
	public void testAutoReconnect(TestContext context) {
		Async nodesCreated = context.strictAsync(2);
		Async connections = context.strictAsync(2);
		Async errors = context.async();

		ws.registerEvents(MeshEvent.NODE_CREATED);
		ws.events().subscribe(event -> nodesCreated.countDown(), context::fail);

		ws.connections()
			.doOnNext(ignore -> connections.countDown())
			// Skip initial connection
			.skip(1)
			.subscribe(ignore -> createBinaryContent().subscribe());

		ws.errors().take(1).subscribe(ignore -> errors.complete());

		createBinaryContent().toCompletable()
			.andThen(stopRestVerticle())
			.andThen(verifyStoppedRestVerticle())
			.andThen(startRestVerticle())
			.subscribe(() -> {}, context::fail);
	}

	@Test
	public void testOneOfHelper(TestContext context) {
		Async async = context.async(2);

		// Register
		ws.registerEvents(NODE_UPDATED);

		// Handle msgs
		ws.events().firstOrError().subscribe(event -> {
			ObjectNode body = event.getBodyAsJson();
			assertNotNull(body.get("uuid").textValue());
			assertEquals("content", body.get("schemaName").textValue());
			async.countDown();
		});

		ws.events().filter(MeshRestClientUtil.isOneOf(NODE_UPDATED)).subscribe(ignore -> async.countDown());
		ws.events().filter(MeshRestClientUtil.isOneOf(NODE_CREATED))
			.subscribe(ignore -> context.fail("No node should have been created"));

		NodeResponse response = call(() -> client().findNodeByUuid(PROJECT_NAME, contentUuid()));
		NodeUpdateRequest request = new NodeUpdateRequest();
		request.getFields().put("slug", FieldUtil.createStringField("blub"));
		request.setVersion(response.getVersion());
		request.setLanguage("en");
		call(() -> client().updateNode(PROJECT_NAME, contentUuid(), request));

		NodeResponse response2 = call(() -> client().findNodeByUuid(PROJECT_NAME, contentUuid()));
		assertNotEquals(response.getVersion(), response2.getVersion());
	}

	@Test
	public void testHeartbeat() throws InterruptedException {
		// Simply tests if the connections has no errors for 10 seconds.

		ws.errors().subscribe(ignore -> fail());

		Thread.sleep(10000);
	}

	/**
	 * Verifies that the rest verticle is actually stopped.
	 * @return
	 */
	private Completable verifyStoppedRestVerticle() {
		return client().me()
			.toCompletable()
			.compose(RxUtil::flip);
	}

}
