/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.as.ui;

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.as.spi.ApplicationServerProvider;
import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.furnace.services.Imported;

/**
 * AS Setup Wizard Implementation.
 * 
 * @author Jeremie Lagarde
 */
public class ASSetupWizardImpl extends AbstractProjectCommand implements ASSetupWizard
{
   @Inject
   @WithAttributes(label = "AS Provider", required = true)
   private UISelectOne<ApplicationServerProvider> provider;

   @Inject
   @WithAttributes(label = "Target Directory")
   private UIInput<String> target;

   @Inject
   private Imported<ApplicationServerProvider> asProviders;

   @Inject
   private ProjectFactory factory;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      provider.setDefaultValue(asProviders.get());
      provider.setValueChoices(asProviders);
      provider.setItemLabelConverter(new Converter<ApplicationServerProvider, String>()
      {
         
         @Override
         public String convert(ApplicationServerProvider source)
         {
            return source == null ? null: source.getName();
         }
      });
      builder.add(provider).add(target);
   }

   @Override
   public void validate(UIValidationContext validator)
   {
      super.validate(validator);
      provider.getValue().validate(validator);
   }

   @Override
   public Metadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("AS: Setup")
               .description("Setup the AS")
               .category(Categories.create("AS", "Setup"));
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      return Results.success();
   }

   @Override
   public NavigationResult next(UINavigationContext context) throws Exception
   {
      ApplicationServerProvider selectedProvider = provider.getValue();
      UIContext uiContext = context.getUIContext();
      uiContext.setAttribute(ApplicationServerProvider.class, selectedProvider);

      // Get the step sequence from the selected application server provider
      List<Class<? extends UICommand>> setupFlow = selectedProvider.getSetupFlow();
      
      // Extract the first command to obtain the next step
      Class<? extends UICommand> next = setupFlow.remove(0);
      Class<?>[] additional = setupFlow.toArray(new Class<?>[setupFlow.size()]);
      return context.navigateTo(next, (Class<? extends UICommand>[]) additional);
   }

   @Override
   protected boolean isProjectRequired()
   {
      return true;
   }

   @Override
   protected ProjectFactory getProjectFactory()
   {
      return factory;
   }
}