package com.gentics.mesh.search.index;

import static com.gentics.mesh.search.index.MappingHelper.NAME_KEY;
import static com.gentics.mesh.search.index.MappingHelper.NOT_ANALYZED;
import static com.gentics.mesh.search.index.MappingHelper.STRING;
import static com.gentics.mesh.search.index.MappingHelper.fieldType;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.gentics.mesh.core.data.Group;
import com.gentics.mesh.core.data.root.RootVertex;

import io.vertx.core.json.JsonObject;

@Component
public class GroupIndexHandler extends AbstractIndexHandler<Group> {

	private static GroupIndexHandler instance;

	@PostConstruct
	public void setup() {
		instance = this;
	}

	public static GroupIndexHandler getInstance() {
		return instance;
	}

	@Override
	protected String getIndex() {
		return "group";
	}

	@Override
	protected String getType() {
		return "group";
	}

	@Override
	protected RootVertex<Group> getRootVertex() {
		return boot.meshRoot().getGroupRoot();
	}

	@Override
	protected Map<String, Object> transformToDocumentMap(Group group) {
		Map<String, Object> map = new HashMap<>();
		map.put(NAME_KEY, group.getName());
		addBasicReferences(map, group);
		return map;
	}

	@Override
	protected JsonObject getMapping() {
		JsonObject props = new JsonObject();
		props.put(NAME_KEY, fieldType(STRING, NOT_ANALYZED));
		return props;
	}

}