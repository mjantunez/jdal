/*
 * Copyright 2009-2012 Jose Luis Martin.
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
package dao.jpa;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.jdal.dao.jpa.JpaUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base test for JpaUtils
 *  
 * @author Jose Luis Martin
 */
@Ignore
public class JpaUtilsTest {

	@PersistenceContext
	EntityManager em;
	
	@Test
	@Transactional
	public void testCountFromCriteria() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Book> bookQuery = cb.createQuery(Book.class);
		Root<Book> root = bookQuery.from(Book.class);
		bookQuery.where(cb.equal(JpaUtils.getPath(root, "author.name"), "Rod"));
		CriteriaQuery<Long> countQuery = JpaUtils.countCriteria(em, bookQuery);
		Long result = Long.valueOf(em.createQuery(bookQuery).getResultList().size());
		
		assertEquals(result, (Long) em.createQuery(countQuery).getSingleResult());
		
	}
	
	@Test
	@Transactional
	public void testCountFromComplexCriteria() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Book> bookQuery = cb.createQuery(Book.class);
		Root<Book> root = bookQuery.from(Book.class);
		Join<Book, Category> join = root.join(em.getMetamodel().entity(Book.class).getSingularAttribute("category", Category.class));
		bookQuery.where(cb.equal(join.<String>get("name"), "Java"));
		bookQuery.from(Author.class);
		bookQuery.select(root);
		CriteriaQuery<Long> countQuery = JpaUtils.countCriteria(em, bookQuery);
		Long result = Long.valueOf(em.createQuery(bookQuery).getResultList().size());
		
		assertEquals(result, (Long) em.createQuery(countQuery).getSingleResult());
	}
	
	@Test
	@Transactional
	public void testInitialize() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Category> c = cb.createQuery(Category.class);
		Root<Category> root = c.from(Category.class);
		c.where(cb.equal(JpaUtils.getPath(root, "name"), "Java"));
		List<Category> list = em.createQuery(c).getResultList();
		Category cat = list.get(0);
		JpaUtils.initialize(em, cat, 2);
		cat.getBooks().contains(new Book());
	}
}
