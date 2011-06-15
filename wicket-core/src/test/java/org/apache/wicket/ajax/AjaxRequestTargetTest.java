/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.ajax;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.MockPageWithLinkAndComponent;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.util.tester.DiffUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the {@link AjaxRequestTarget}.
 * 
 * @author Frank Bille
 */
public class AjaxRequestTargetTest extends WicketTestCase
{
	private static final Logger log = LoggerFactory.getLogger(AjaxRequestTargetTest.class);
	
	/**
	 * Test that a normal <style> header contribution is added correctly.
	 * 
	 * @throws IOException
	 */
	public void testHeaderContribution1() throws IOException
	{
		executeHeaderTest(MockComponent1.class, "MockComponent1-expected.html");
	}

	/**
	 * Test that if there are no headers contributed in any components added to the response, we
	 * then don't add <header-contribution> at all.
	 * 
	 * @throws IOException
	 */
	public void testHeaderContribution2() throws IOException
	{
		executeHeaderTest(MockComponent2.class);
	}

	/**
	 * Test that a link with a wicket:id is added correctly.
	 * 
	 * @throws IOException
	 */
	public void testHeaderContribution3() throws IOException
	{
		executeHeaderTest(MockComponent3.class, "MockComponent3-expected.html");
	}

	private <C extends Component> void executeHeaderTest(final Class<C> componentClass)
		throws IOException
	{
		executeHeaderTest(componentClass, null);
	}

	private <C extends Component> void executeHeaderTest(final Class<C> componentClass,
		String expectedFile) throws IOException
	{
		final MockPageWithLinkAndComponent page = new MockPageWithLinkAndComponent();

		page.add(new WebComponent(MockPageWithLinkAndComponent.COMPONENT_ID).setOutputMarkupId(true));

		page.add(new AjaxLink<Void>(MockPageWithLinkAndComponent.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				// Create an instance of the component
				try
				{
					Constructor<? extends Component> con = componentClass.getConstructor(new Class[] { String.class });

					Component comp = con.newInstance(MockPageWithLinkAndComponent.COMPONENT_ID);
					page.replace(comp);
					comp.setOutputMarkupId(true);

					target.add(comp);
				}
				catch (Exception e)
				{
					log.error(e.getMessage(), e);
				}
			}
		});

		tester.startPage(page);

		// System.out.println(tester.getServletResponse().getDocument());
		tester.debugComponentTrees();

		tester.clickLink(MockPageWithLinkAndComponent.LINK_ID);

		String document = tester.getLastResponseAsString();
		String headerContribution = null;

		Pattern pat = Pattern.compile(".*<header-contribution.*?>(.*?)</header-contribution>.*",
			Pattern.DOTALL);
		Matcher mat = pat.matcher(document);
		if (mat.matches())
		{
			headerContribution = mat.group(1);
		}

		// If the filename is empty we use it to say that the headerContribution
		// should be empty.
		// This means that it doesn't exist at all
		if (expectedFile == null)
		{
			assertNull("There was a header contribution on the response " +
				"(though we didn't expect one): <" + headerContribution + ">", headerContribution);
		}
		else if (headerContribution == null)
		{
			fail("Failed to find header contribution: \n" + document);
		}
		else
		{
			DiffUtil.validatePage(headerContribution, getClass(), expectedFile, true);
		}
	}

	/**
	 * WICKET-2328
	 */
	public void testRenderMyPage()
	{
		// start and render the test page
		tester.startPage(HomePage2.class);

		// assert rendered page class
		tester.assertRenderedPage(HomePage2.class);

		// assert rendered label component
		tester.assertLabel("msg", "onBeforeRender called");

		// click the ajax link; this should have no effect
		tester.clickLink("link");

		// assert rendered label component again to make sure
		// THIS FAILS! even though the same sequence of clicks
		// done in a browser does not cause the label to change
		tester.assertLabel("msg", "onBeforeRender called");
	}

	/**
	 * WICKET-2543
	 */
	public void testVarargsAddComponent()
	{
		tester.startPage(VarargsAddComponentPage.class);

		for (int i = 0; i < VarargsAddComponentPage.NUMBER_OF_LABELS; i++)
		{
			final String labelMarkupId = "label" + i;
			final String expectedContent = String.format(VarargsAddComponentPage.INITIAL_CONTENT, i);
			tester.assertLabel(labelMarkupId, expectedContent);
		}

		tester.clickLink("link");

		for (int i = 0; i < VarargsAddComponentPage.NUMBER_OF_LABELS; i++)
		{
			final String labelMarkupId = "label" + i;
			final String expectedContent = String.format(VarargsAddComponentPage.INITIAL_CONTENT, i) +
				VarargsAddComponentPage.AJAX_APPENDED_SUFFIX;
			tester.assertLabel(labelMarkupId, expectedContent);
		}
	}

	/**
	 * Testing the default event raised whenever Wicket begins to create an AJAX response
	 */
	public void testDefaultEventRaisedOnAjaxResponse()
	{
		tester.startPage(TestEventPage.class);
		tester.clickLink(MockPageWithLinkAndComponent.LINK_ID, true);
		TestEventPage page = (TestEventPage)tester.getLastRenderedPage();
		assertTrue(page.defaultEventRaised);
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3263">WICKET-3263</a>
	 */
	public void testGlobalAjaxRequestTargetListeners()
	{
		final ValidatingAjaxRequestTargetListener listener = new ValidatingAjaxRequestTargetListener();

		tester.getApplication().getAjaxRequestTargetListeners().add(listener);

		tester.startPage(TestEventPage.class);
		tester.clickLink(MockPageWithLinkAndComponent.LINK_ID, true);

		assertTrue(listener.onBeforeRespondExecuted);
		assertTrue(listener.onAfterRespondExecuted);
	}

	private static class ValidatingAjaxRequestTargetListener implements AjaxRequestTarget.IListener
	{
		boolean onBeforeRespondExecuted = false;
		boolean onAfterRespondExecuted = false;

		public void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget target)
		{
			onBeforeRespondExecuted = true;

		}

		public void onAfterRespond(Map<String, Component> map,
			AjaxRequestTarget.IJavaScriptResponse response)
		{
			onAfterRespondExecuted = true;
		}
	}

	/**
	 */
	public static class TestEventPage extends MockPageWithLinkAndComponent
	{
		private static final long serialVersionUID = 1L;

		boolean defaultEventRaised = false;

		/**
		 */
		public TestEventPage()
		{
			add(new AjaxLink<Void>(LINK_ID)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
				}
			});
			add(new WebComponent(COMPONENT_ID)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onEvent(IEvent<?> event)
				{
					if (event.getPayload() instanceof AjaxRequestTarget)
					{
						defaultEventRaised = true;
					}
				}
			});
		}
	}
}
