/*
 * Copyright 2008-2010 the original author or authors.
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
package org.jdal.dao.filter;

import org.jdal.dao.BeanFilter;
import org.jdal.reporting.ReportType;

/**
 * Report Filter 
 * 
 * @author Jose Luis Martin - (jlm@joseluismartin.info)
 */
public class ReportFilter extends BeanFilter {
	
	/** filter name */
	private String name;
	/** filter type */
	private ReportType type;
	
	
	public ReportFilter(){
		this("report");
	}
	
	public ReportFilter(String name){
		setFilterName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ReportType getType() {
		return type;
	}

	public void setType(ReportType type) {
		this.type = type;
	}
}
