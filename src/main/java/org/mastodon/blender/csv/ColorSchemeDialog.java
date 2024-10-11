/*-
 * #%L
 * A Mastodon plugin data allows to show the embryo in Blender.
 * %%
 * Copyright (C) 2022 - 2024 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.blender.csv;

import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Spot;

/**
 * A JComboBox for selecting a color scheme.
 * <br>
 * This class provides can show an option dialog to let the user select a coloring scheme from a list,
 * including tag sets and feature color modes.
 */
public class ColorSchemeDialog
{
	private ColorSchemeDialog()
	{
		// prevent instantiation
	}

	private static final String DEFAULT_OPTION = "[no coloring scheme]";

	private static final String TAGS_HEADER = "Tags:";

	private static final String FEATURE_COLOR_MODES_HEADER = "Feature Color Modes:";

	private static final String SEPARATOR = "-----------------";

	/**
	 * Show a dialog to let the user select a coloring scheme.
	 * @param projectModel the project model
	 * @return the name of the selected coloring scheme, or {@code null} if none was selected.
	 */
	public static String showDialog( ProjectModel projectModel )
	{
		JComboBox< ColorFunction > comboBox = getColorSchemesComboBox( projectModel );

		int result = JOptionPane.showOptionDialog(
				null,
				comboBox,
				"Select coloring scheme",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				DEFAULT_OPTION
		);

		if ( result == JOptionPane.OK_OPTION )
		{
			ColorFunction selectedItem = ( ColorFunction ) comboBox.getSelectedItem();
			if ( selectedItem == null )
				return null;
			if ( selectedItem.getGroup() == null )
				return null;
			return selectedItem.toString();
		}
		return null;
	}

	private static JComboBox< ColorFunction > getColorSchemesComboBox( ProjectModel projectModel )
	{
		List< ColorFunction > options = getColorSchemeOptions( projectModel );
		JComboBox< ColorFunction > comboBox = new JComboBox<>( options.toArray( new ColorFunction[ 0 ] ) );
		comboBox.setRenderer( new ColorSchemeOptionsRenderer() );
		return comboBox;
	}

	static List< ColorFunction > getColorSchemeOptions( ProjectModel projectModel )
	{
		List< ColorFunction > colorFunctions = new ArrayList<>();
		colorFunctions.add( new EmptyColorFunction( DEFAULT_OPTION ) );
		colorFunctions.add( new EmptyColorFunction( SEPARATOR ) );
		colorFunctions.add( new EmptyColorFunction( TAGS_HEADER ) );
		colorFunctions.addAll( GraphToCsvUtils.getTagSetColorFunctions( projectModel ) );
		colorFunctions.add( new EmptyColorFunction( SEPARATOR ) );
		colorFunctions.add( new EmptyColorFunction( FEATURE_COLOR_MODES_HEADER ) );
		colorFunctions.addAll( GraphToCsvUtils.getFeatureColorFunctions( projectModel ) );
		return colorFunctions;
	}

	private static class ColorSchemeOptionsRenderer implements ListCellRenderer< ColorFunction >
	{

		private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		@Override
		public Component getListCellRendererComponent( final JList< ? extends ColorFunction > list, final ColorFunction colorFunction,
				final int index, final boolean isSelected, final boolean cellHasFocus )
		{
			if ( colorFunction instanceof EmptyColorFunction )
			{
				String name = colorFunction.toString();
				switch ( name )
				{
				case TAGS_HEADER:
				case FEATURE_COLOR_MODES_HEADER:
					JLabel label =
							( JLabel ) defaultRenderer.getListCellRendererComponent( list, colorFunction, index, false, cellHasFocus );
					label.setFont( label.getFont().deriveFont( Font.BOLD ) );
					return label;
				case SEPARATOR:
					return new JSeparator( SwingConstants.HORIZONTAL );
				default:
					// use default entry rendering
				}
			}
			JLabel defaultEntry =
					( JLabel ) defaultRenderer.getListCellRendererComponent( list, colorFunction, index, isSelected, cellHasFocus );
			defaultEntry.setFont( defaultEntry.getFont().deriveFont( Font.PLAIN ) );
			return defaultEntry;
		}
	}

	private static class EmptyColorFunction implements ColorFunction
	{
		private final String name;

		private EmptyColorFunction( String name )
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}

		@Override
		public Group getGroup()
		{
			return null;
		}

		@Override
		public Integer apply( final Spot spot )
		{
			return null;
		}
	}
}
