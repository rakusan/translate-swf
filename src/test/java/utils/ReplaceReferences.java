/*
 * DebugScript.java
 * Translate SWF
 *
 * Copyright (c) 2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package utils;

import java.util.Iterator;
import java.util.List;

import com.flagstone.transform.action.Action;
import com.flagstone.transform.action.NewFunction;
import com.flagstone.transform.action.Push;
import com.flagstone.transform.action.Table;
import com.flagstone.transform.action.TableIndex;

public class ReplaceReferences {

	public static void replaceReferences(List<Action>list) {
		updateActions(extractTable(list), list);
	}

	private static Table extractTable(final List<Action> list) {
		Table table = new Table();
		Action action;

		for (Iterator<Action> iter = list.iterator(); iter.hasNext();) {
			action = iter.next();
			if (action instanceof Table) {
				table = (Table)action;
				iter.remove();
			}
		}
		return table;
	}

	private static void updateActions(final Table table, final List<Action> list) {
		Action action;
		for (int i = 0; i < list.size(); i++) {
			action = list.get(i);
			if (action instanceof Push) {
				list.set(i, replaceAction(table, (Push) action));
			} else if (action instanceof NewFunction) {
				list.set(i, replaceAction(table, (NewFunction) action));
			}
		}
	}

	private static Push replaceAction(final Table table, final Push action) {
		List<Object>values = action.getValues();
		replaceTableIndex(table, values);
		return new Push(values);
	}

	private static NewFunction replaceAction(final Table table,
			final NewFunction action) {
		List<Action>actions = action.getActions();
		updateActions(table, actions);
		return new NewFunction(action.getName(),
				action.getArguments(), actions);
	}

	private static void replaceTableIndex(final Table table,
			final List<Object> list) {
		Object value;
		int index;
		for (int j = 0; j < list.size(); j++) {
			value = list.get(j);
			if (value instanceof TableIndex) {
				index = ((TableIndex)value).getIndex();
				list.set(j, table.getValues().get(index));
			}
		}
	}
}
