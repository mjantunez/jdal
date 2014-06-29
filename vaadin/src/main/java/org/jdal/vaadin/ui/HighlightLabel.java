/*
 * Copyright 2009-2014 the original author or authors.
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
package org.jdal.vaadin.ui;

import ch.qos.logback.classic.pattern.color.HighlightingCompositeConverter;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.shared.communication.SharedState;
import com.vaadin.shared.ui.JavaScriptComponentState;
import com.vaadin.ui.AbstractJavaScriptComponent;

/**
 * Label with highlight.js support 
 * 
 * @author Jose Luis Martin
 * @since 2.1
 */
@JavaScript({"http://yandex.st/highlightjs/8.0/highlight.min.js", "highlightlabel.js"})
@StyleSheet("http://yandex.st/highlightjs/8.0/styles/default.min.css")
public class HighlightLabel extends AbstractJavaScriptComponent {
	
	public void setValue(String value) {
		getState().xhtml = value;
	}

	@Override
	protected HighlightLabelState getState() {
		return (HighlightLabelState) super.getState();
	}
	
	@Override
	protected SharedState createState() {
		return new HighlightLabelState();
	}
	
	
	public static class HighlightLabelState extends JavaScriptComponentState {
		public String xhtml;
	}

}
