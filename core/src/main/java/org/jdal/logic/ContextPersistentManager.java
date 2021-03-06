/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdal.logic;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jdal.dao.Dao;
import org.jdal.dao.Page;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Bulk all generics in one object to avoid excesive parametrization.
 * 
 * @author Jose Luis Martin 
 */
public class ContextPersistentManager implements Dao<Object, Serializable> {

	@Autowired
	@SuppressWarnings("rawtypes")
	private List<Dao> services;
	private Map<Class<?>, Dao<Object, Serializable>> serviceMap = 
			new ConcurrentHashMap<Class<?>, Dao<Object, Serializable>>();
	

	@SuppressWarnings("unchecked")
	public void init() {
		for (Dao<Object, Serializable> ps : services) {
			if (ps.getEntityClass() != null)
				serviceMap.put(ps.getEntityClass(), ps);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public <K> Page<K> getPage(Page<K> page) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Serializable> getKeys(Page<Object> page) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object initialize(Object entity, int depth) {
		return getDao(entity.getClass()).initialize(entity);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object initialize(Object entity) {
		return getDao(entity.getClass()).initialize(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object save(Object entity) {
		return getDao(entity.getClass()).save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete(Object entity) {
		getDao(entity.getClass()).delete(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteById(Serializable id) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Object> getAll() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<Object> save(Collection<Object> collection) {
		if (collection.isEmpty()) 
			return collection;
		
		return getDao(collection.iterator().next().getClass()).save(collection);
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete(Collection<Object> collection) {
		if (!collection.isEmpty())
			getDao(collection.iterator().next().getClass()).delete(collection);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteById(Collection<Serializable> ids) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object get(Serializable id) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public <E> E get(Serializable id, Class<E> clazz) {
		return getDao(clazz).get(id, clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	public <E> List<E> getAll(Class<E> clazz) {
		return getDao(clazz).getAll(clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<Object> getEntityClass() {
		return Object.class;
	}
	
	/**
	 * @param entity
	 * @return
	 */
	private Dao<Object, Serializable> getDao(Class<?> clazz) {
		return serviceMap.get(clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists(Serializable id) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> findByNamedQuery(String queryName,
			Map<String, Object> queryParams) {
		throw new UnsupportedOperationException();
	}

}
