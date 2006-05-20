/*
 * $Id: WebMarkupContainerWithAssociatedMarkup.java,v 1.4 2006/03/04 08:49:02
 * jdonnerstag Exp $ $Revision: 1.1 $ $Date: 2006/03/10 22:31:50 $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.list;

import java.util.List;

import wicket.model.BoundCompoundPropertyModel;
import wicket.model.IModel;

/**
 * Simple ListVew subclass that wraps its item models in a
 * BoundCompoundPropertyModel. Useful for lists where the item components will
 * be mapped through property expressions.
 * 
 * @author Nathan Hamblen
 */
public abstract class PropertyListView extends ListView
{
	/**
	 * Construct without model, assume bound externally.
	 * 
	 * @param id
	 *            Wicket id
	 */
	public PropertyListView(final String id)
	{
		super(id);
	}

	/**
	 * Construct with a model.
	 * 
	 * @param id
	 *            Wicket id
	 * @param model
	 *            wrapping a List
	 */
	public PropertyListView(final String id, final IModel model)
	{
		super(id, model);
	}

	/**
	 * Construct with a "small," unmodeled List. The object can not be detached
	 * and will reside in the session, but is convenient for lists of a limited
	 * size.
	 * 
	 * @param id
	 *            Wicket id
	 * @param list
	 *            unmodeled List
	 */
	public PropertyListView(final String id, final List list)
	{
		super(id, list);
	}

	/**
	 * Wraps a ListItemModel in a BoundCompoundPropertyModel.
	 * 
	 * @param model
	 * @param index
	 * @return a BoundCompoundPropertyModel wrapping a ListItemModel
	 */
	@Override
	protected IModel getListItemModel(final IModel model, final int index)
	{
		return new BoundCompoundPropertyModel(super.getListItemModel(model, index));
	}
}
