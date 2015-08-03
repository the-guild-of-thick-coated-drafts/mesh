package com.gentics.mesh.core.data.root.impl;

import static com.gentics.mesh.core.data.relationship.MeshRelationships.HAS_PROJECT;

import com.gentics.mesh.core.data.Project;
import com.gentics.mesh.core.data.User;
import com.gentics.mesh.core.data.impl.ProjectImpl;
import com.gentics.mesh.core.data.root.ProjectRoot;

public class ProjectRootImpl extends AbstractRootVertex<Project> implements ProjectRoot {

	@Override
	protected Class<? extends Project> getPersistanceClass() {
		return ProjectImpl.class;
	}

	@Override
	protected String getRootLabel() {
		return HAS_PROJECT;
	}

	@Override
	public void addProject(Project project) {
		addItem(project);
	}

	@Override
	public void removeProject(Project project) {
		removeItem(project);
	}

	// TODO unique

	@Override
	public Project create(String name, User creator) {
		Project project = getGraph().addFramedVertex(ProjectImpl.class);
		project.setName(name);
		project.getNodeRoot();
		project.createBaseNode(creator);

		project.setCreator(creator);
		project.setCreationTimestamp(System.currentTimeMillis());
		project.setEditor(creator);
		project.setLastEditedTimestamp(System.currentTimeMillis());

		project.getTagRoot();
		project.getSchemaRoot();
		project.getTagFamilyRoot();

		addItem(project);

		return project;
	}

}
