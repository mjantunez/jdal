/*
 * Copyright 2009-2011 Jose Lus Martin.
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
package org.jdal.reporting.datasource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

import org.jdal.dao.Page;
import org.jdal.dao.PageableDataSource;

/**
 * Adapter to use PageableDataSource and Pages as JRDataSource.
 * 
 * @author Jose Luis Martin - (jlm@joseluismartin.info)
 */
public class PageJRDatasourceAdapter extends JRAbstractBeanDataSource {
	
	private Page<Object> page;
	private Object currentObject;
	private int index = 0;
	
	/**
	 * 
	 */
	public PageJRDatasourceAdapter(PageableDataSource<Object> ds) {
		this(true);
		page = new Page<Object>();
		page.setPageableDataSource(ds);
		
	}
	
	/**
	 * @param isUseFieldDescription
	 */
	public PageJRDatasourceAdapter(boolean isUseFieldDescription) {
		super(isUseFieldDescription);
	}

	/**
	 * {@inheritDoc}
	 */
	public void moveFirst() throws JRException {
		page.firstPage();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getFieldValue(JRField field) throws JRException {
		try {
			return getBeanProperty(currentObject, field.getName());
		} catch (Exception e) {
			// rethrow
			throw new JRException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean next() throws JRException {
		if (getCurrentObject()) {
			return true;
		}
	
		if (page.hasNext()) {
			page.nextPage();
			index = 0;
			return getCurrentObject();
		}
		
		return false;
		
	}

	private boolean getCurrentObject() {
		if (index < page.getData().size()) {
			currentObject = page.getData().get(index++);
			return true;
		}
		
		return false;
	}

	/**
	 * @return the page
	 */
	public Page<Object> getPage() {
		return page;
	}

	/**
	 * @param page the page to set
	 */
	public void setPage(Page<Object> page) {
		this.page = page;
		// ensure that data is loaded
		if (page.getData() == null || page.getData().size() == 0)
			page.load();
	}
}
