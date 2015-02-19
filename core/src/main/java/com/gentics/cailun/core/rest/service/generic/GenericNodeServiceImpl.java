package com.gentics.cailun.core.rest.service.generic;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gentics.cailun.core.repository.I18NValueRepository;
import com.gentics.cailun.core.repository.generic.GenericNodeRepository;
import com.gentics.cailun.core.rest.model.generic.GenericNode;

@Component
@Transactional
public class GenericNodeServiceImpl<T extends GenericNode> implements GenericNodeService<T> {

	@Autowired
	I18NValueRepository i18nPropertyRepository;

	@Autowired
	@Qualifier("genericNodeRepository")
	GenericNodeRepository<T> nodeRepository;

	@Override
	public T save(T node) {
		return nodeRepository.save(node);
	}

	@Override
	public void delete(T node) {
		nodeRepository.delete(node);
	}

	@Override
	public T findOne(Long id) {
		return nodeRepository.findOne(id);
	}

	@Override
	public void save(List<T> nodes) {
		this.nodeRepository.save(nodes);
	}

	@Override
	public Result<T> findAll() {
		return nodeRepository.findAll();

	}

	@Override
	public Result<T> findAll(String project) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T findByName(String project, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T findByUUID(String project, String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

}
