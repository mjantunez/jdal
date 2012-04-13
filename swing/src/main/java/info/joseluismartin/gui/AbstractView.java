/*
 * Copyright 2009-2011 original author or authors.
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
package info.joseluismartin.gui;

import info.joseluismartin.gui.bind.BinderFactory;
import info.joseluismartin.gui.bind.BinderHolder;
import info.joseluismartin.gui.bind.CompositeBinder;
import info.joseluismartin.gui.bind.ConfigurableBinderFactory;
import info.joseluismartin.gui.bind.ConfigurableControlAccessorFactory;
import info.joseluismartin.gui.bind.ControlAccessor;
import info.joseluismartin.gui.bind.ControlAccessorFactory;
import info.joseluismartin.gui.bind.ControlChangeListener;
import info.joseluismartin.gui.bind.ControlError;
import info.joseluismartin.gui.bind.ControlEvent;
import info.joseluismartin.gui.bind.ControlInitializer;
import info.joseluismartin.gui.bind.DirectFieldAccessor;
import info.joseluismartin.gui.bind.PropertyBinder;
import info.joseluismartin.gui.validation.ErrorProcessor;

import java.awt.Component;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

/**
 * Template class that simplifies {@link View} implementation.
 * 
 * <p> The central method is <code>buildPanel</code> that builds the <code>JComponent</code>
 * that hold the view controls. You may use custom binding of the view overwriting the methods 
 * <code>doUpdate</code> and <code>doRefresh</code>.
 * 
 * <p> For common binding code, you usually use autobinding facitility, that is, using 
 * the same name to the field control and model property name, and setting autobinding to true.
 * When using autobinding, you may exclude model properties from binding using some
 * of <code>ignoreProperty</code> methods.
 * 
 * <p> Manual binding is also supported via <code>bind</code> methods. When binding a control, a
 * <code>StateChangeListener</code> is added to the control for setting dirty property on control
 * changes.
 * 
 * <p> Only <code>org.springframework.util.validation.Validator</code> validators are supported
 * The <code>validateView</code> method calls configured
 * <code>ErrorProcessors</code> to process errors found in validation.
 * 
 * @author Jose Luis Martin - (jlm@joseluismartin.info)
 * @since 1.0
 * @see BinderFactory
 * @see ControlAccessorFactory
 * @see ErrorProcessor
 */
public abstract class AbstractView<T> implements View<T>, ControlChangeListener, BinderHolder {
	
	public final static String DEFAULT_BINDER_FACTORY_NAME = "binderFactory";
	/** log */
	private static final Log log = LogFactory.getLog(AbstractView.class);
	/** view name */
	private String name;
	/** binder factory to make property binders */
	private BinderFactory binderFactory;
	/** control accessor factory */
	private ControlAccessorFactory controlAccessorFactory;
	/** hold binders */
	private CompositeBinder<T> binder = new CompositeBinder<T>();
	/** if true, do an automatic binding using property names */
	private boolean autobinding = false;
	/** Set with property names to ingnore on binding commands */
	private Set<String> ignoredProperties = new HashSet<String>();
	/** data model */
	private T model;
	/** JComponent that hold controls */
	private JComponent panel;
	/** subviews list */
	private List<View<T>> subViews = new ArrayList<View<T>>();
	/** validator to check binding and model values */
	private Validator validator;
	/** message source for internationalization */
	@Autowired
	protected MessageSource messageSource;
	/** List of error handlers */
	private List<ErrorProcessor> errorProcessors = new ArrayList<ErrorProcessor>();
	/** Validation Errors */
	protected BindingResult errors;
	/** dirty state */
	private boolean dirty = false;
	/** initialize controls on autobind **/
	private boolean initializeControls = true;
	
	protected int width = 0;
	protected int height = 0;
	/** control initializer */
	private ControlInitializer controlInitializer;
	
	/**
	 * Default ctor
	 */
	public AbstractView() {
		this(null);
	}
	
	/**
	 * Create the view and set the model
	 * @param model model to set
	 */
	public AbstractView(T model) {
		setModel(model);
	}
	
	/**
	 * add a binding for control and model property name
	 * @param component control
	 * @param propertyName the model property path to bind
	 * @param readOnly if true, binding only do refresh()
	 */
	public void bind(Object component, String propertyName, boolean readOnly) {
		checkFactories();
		binder.bind(component, propertyName, readOnly);
		listen(component);
		
	}
	
	/**
	 * Check if there are binder and control accessor factories configured, if not
	 * use defaults.
	 */
	private void checkFactories() {
		if (controlAccessorFactory == null) {
			setControlAccessorFactory(ConfigurableControlAccessorFactory.getDefaultFactory());
		}
		if (binderFactory == null) {
			setBinderFactory(ConfigurableBinderFactory.getDefaultFactory());
		}
	}

	/**
	 * add a binding for control and model property name
	 * @param component control
	 * @param propertyName the model property path to bind
	 */
	public void bind(Object component, String propertyName) {
		bind(component, propertyName, false);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public JComponent getPanel() {
		if (panel == null) {
			panel = buildPanel();
			if (width != 0 && height != 0)
				panel.setSize(width, height);
			
		}
		return panel;
	}
	
	/**
	 * Build the JComponent that hold controls.
	 * @return a JCompoent
	 */
	protected abstract JComponent buildPanel();

	
	/**
	 * {@inheritDoc}
	 */
	public T getModel() {
		return model;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final void setModel(T model) {
		this.model = model;
		createBindingResult();
		binder.setModel(model);
		
		// refresh subviews
		for (View<T> v : subViews)
			v.setModel(model);
		
		onSetModel(model);
	}
	
	/**
	 * 
	 */
	private void createBindingResult() {
		if (model != null)
			errors = new BeanPropertyBindingResult(getModel(), getModel().getClass().getSimpleName());
	}

	/**
	 * Callback method to handle model changes
	 * @param model the new model
	 */
	protected void onSetModel(T model) {
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final void update() {
		clearErrors();
		// do custom update
		doUpdate();
		
		// update binder
		binder.update();
		
		if (errors != null && binder.getBindingResult() != null &&
				errors.getObjectName().equals(binder.getBindingResult().getObjectName()))
			errors.addAllErrors(binder.getBindingResult());
		
		// update subviews
		for (View<T>  v : subViews) {
			v.update();
			if (errors != null && v.getBindingResult() != null &&
					errors.getObjectName().equals(v.getBindingResult().getObjectName()))
				errors.addAllErrors(v.getBindingResult());
		}
		
	
	}

	/**
	 * Clear validation erros
	 */
	private void clearErrors() {
		if (getModel() != null && errors != null && errors.hasErrors())
			createBindingResult();
		resetErrorProcessors();
		
	}

	/**
	 * Callback method on update()
	 */
	protected void doUpdate() {
		
	}
	
	/**
	 * Add a subview, the subview is refreshed, updated and hold the same model
	 * that this view, for adding views with other models, use bind()
	 * @param view
	 */
	public void addView(View<T> view) {
		subViews.add(view);
		view.setModel(model);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final void refresh() {
		clearErrors();
		doRefresh();
		binder.refresh();

		// refresh subviews
		for (View<T> v : subViews)
			v.refresh();
	}
	
	/**
	 * Callback method for refresh()
	 */
	protected void doRefresh() {
		
	}

	/**
	 * Listen control for changes.
	 */
	public void listen(Object control) {
		ControlAccessor c = controlAccessorFactory.getControlAccessor(control);
		if (c != null) {
			c.addControlChangeListener(this);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void controlChange(ControlEvent e) {
		setDirty(true);
	}
	
	/**
	 * Gets the binder factory
	 * @return the binder factory
	 */
	public BinderFactory getBinderFactory() {
		return binderFactory;
	}
	
	

	/**
	 * Sets the binder factory, propagate it to composite binder.
	 * @param binderFactory to set
	 */
	public void setBinderFactory(BinderFactory binderFactory) {
		this.binderFactory = binderFactory;
		binder.setBinderFactory(binderFactory);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean validateView() {
		if (validator == null && !errors.hasErrors())
			return true;
		
		if (validator != null) 
			validator.validate(getModel(), errors);
		
		if (errors.hasErrors()) {
			for (FieldError error : errors.getFieldErrors()) {
				for (ErrorProcessor ep : errorProcessors ) {
					if (error instanceof ControlError) {
						ControlError ce = (ControlError) error;
						ep.processError(ce.getComponent(), error);
					}
					else {
						Binder<?> b = binder.getBinder(error.getField());
						if (b instanceof PropertyBinder) {
							ep.processError(((PropertyBinder) b).getComponent(), error);
						}
					}
				}
			}
			return false;
		}
		return true;
	}
	
	private void resetErrorProcessors() {
		for (ErrorProcessor ep : errorProcessors) {
			ep.reset();
		}
		
	}

	/**
	 * Build a error message with all errors.
	 * @return String with error message
	 */
	public String getErrorMessage() {
		StringBuilder sb = new StringBuilder();
		if (errors.hasErrors()) {
			sb.append("\n");
			Iterator<ObjectError> iter = errors.getAllErrors().iterator();
			while (iter.hasNext()) {
				ObjectError oe = (ObjectError) iter.next();
				sb.append("- ");
				sb.append(getMessage(oe));
				sb.append("\n");
			}
		}
		sb.append("\n");
		return sb.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void clear() {
		T model = getModel();
		if (model != null) {
			try {
				setModel((T) model.getClass().newInstance());
				refresh();
			} catch (InstantiationException e) {
				log.error(e);
			} catch (IllegalAccessException e) {
				log.error(e);
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void enableView(boolean enabled) {
		for (Binder<?> b : binder.getPropertyBinders()) {
			Object control = ((PropertyBinder) b).getComponent();
			
			if (control instanceof Component) 
				((Component) control).setEnabled(enabled);
			else if (control instanceof View<?>) 
				((View<?>) control).enableView(enabled);
			
			for (View<?> v : subViews) 
				v.enableView(enabled);
		}
	}
	
	/**
	 * Bind Controls with the same name that a property in the model.
	 */
	public void autobind() {
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(getModel());
		PropertyAccessor  viewPropertyAccessor = new DirectFieldAccessor(this);
		// iterate on model properties
		for (PropertyDescriptor pd : bw.getPropertyDescriptors()) {
			String propertyName = pd.getName();
			if ( !ignoredProperties.contains(propertyName) && viewPropertyAccessor.isReadableProperty(propertyName)) {
				Object control = viewPropertyAccessor.getPropertyValue(propertyName);
				if (control != null) {
					if (log.isDebugEnabled()) 
						log.debug("Found control: " + control.getClass().getSimpleName() + 
								" for property: " + propertyName);
					// do bind
					bind(control, propertyName);
					// initialize control
					if (isInitializeControls() && controlInitializer != null)
						controlInitializer.initialize(control, propertyName, getModel().getClass());
				}
			}
		}
	}
	
	/** 
	 * I18n Support
	 * @param code message code
	 * @return message or code if none defined
	 */
	protected String getMessage(String code) {
		try {
			return messageSource == null ?
				code : messageSource.getMessage(code, null, Locale.getDefault());
		} 
		catch (NoSuchMessageException nsme) {
			log.error(nsme);
		}
		
		return code;
	}
	
	/** 
	 * I18n Support
	 * @param msr message source resolvable
	 * @return message or code if none defined
	 */
	protected String getMessage(MessageSourceResolvable msr) {
		return messageSource == null ?
				msr.getDefaultMessage() : messageSource.getMessage(msr, Locale.getDefault());
	}
	
	/**
	 * Add a property name  to ignore on binding.
	 * @param propertyName property name to ignore
	 */
	public void ignoreProperty(String propertyName) {
		ignoredProperties.add(propertyName);
	}
	
	

	/**
	 * @return the ignoredProperties
	 */
	public Set<String> getIgnoredProperties() {
		return ignoredProperties;
	}

	/**
	 * @param ignoredProperties the ignoredProperties to set
	 */
	public void setIgnoredProperties(Set<String> ignoredProperties) {
		this.ignoredProperties = ignoredProperties;
	}

	/**
	 * Add a Collection of property names to ignore on binding
	 * @param c Collection of property names.
	 */
	public void ignoreProperties(Collection<? extends String> c) {
		ignoredProperties.addAll(c);
	}

	/**
	 * @return the validator
	 */
	public Validator getValidator() {
		return validator;
	}

	/**
	 * @param validator the validator to set
	 */
	public void setValidator(Validator validator) {
		this.validator = validator;
	}
	

	/**
	 * @return the messageSource
	 */
	public MessageSource getMessageSource() {
		return messageSource;
	}

	/**
	 * @param messageSource the messageSource to set
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the errorProcessors
	 */
	public List<ErrorProcessor> getErrorProcessors() {
		return errorProcessors;
	}

	/**
	 * @param errorProcessors the errorProcessors to set
	 */
	public void setErrorProcessors(List<ErrorProcessor> errorProcessors) {
		this.errorProcessors = errorProcessors;
	}

	/**
	 * @return the dirty
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * @param dirty the dirty to set
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	/**
	 * @return the controlAccessorFactory
	 */
	public ControlAccessorFactory getControlAccessorFactory() {
		return controlAccessorFactory;
	}

	/**
	 * @param controlAccessorFactory the controlAccessorFactory to set
	 */
	public void setControlAccessorFactory(ControlAccessorFactory controlAccessorFactory) {
		this.controlAccessorFactory = controlAccessorFactory;
	}

	/**
	 * @return the autobinding
	 */
	public boolean isAutobinding() {
		return autobinding;
	}

	/**
	 * @param autobinding the autobinding to set
	 */
	public void setAutobinding(boolean autobinding) {
		this.autobinding = autobinding;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public BindingResult getBindingResult() {
		if (errors == null && getModel() != null)
			createBindingResult();
		
		return errors;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyBinder getBinder(String propertyName) {
		return binder.getBinder(propertyName);
	}

	/**
	 * @return the initializeControls
	 */
	public boolean isInitializeControls() {
		return initializeControls;
	}

	/**
	 * @param initializeControls the initializeControls to set
	 */
	public void setInitializeControls(boolean initializeControls) {
		this.initializeControls = initializeControls;
	}

	/**
	 * @return the controlInitializer
	 */
	public ControlInitializer getControlInitializer() {
		return controlInitializer;
	}

	/**
	 * @param controlInitializer the controlInitializer to set
	 */
	public void setControlInitializer(ControlInitializer controlInitializer) {
		this.controlInitializer = controlInitializer;
	}
}
