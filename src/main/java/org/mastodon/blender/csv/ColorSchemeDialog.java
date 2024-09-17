package org.mastodon.blender.csv;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.feature.FeatureColorMode;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;

import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
	 * @param coloringModel the coloring model
	 * @return the name of the selected coloring scheme, or {@code null} if none was selected.
	 */
	public static String showDialog( ProjectModel projectModel, ColoringModel coloringModel )
	{
		JComboBox< String > comboBox = getColorSchemesComboBox( projectModel, coloringModel );

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
			String selectedItem = (String) comboBox.getSelectedItem();
			if ( selectedItem == null)
				return null;

			switch ( selectedItem )
			{
			case TAGS_HEADER:
			case FEATURE_COLOR_MODES_HEADER:
			case SEPARATOR:
				return null;
			default:
				return selectedItem;
			}
		}
		return null;
	}

	private static JComboBox< String > getColorSchemesComboBox( ProjectModel projectModel, ColoringModel coloringModel )
	{
		List< String > options = getColorSchemeOptions( projectModel, coloringModel );
		JComboBox< String > comboBox = new JComboBox<>( options.toArray(new String[0]) );
		comboBox.setRenderer( new ColorSchemeOptionsRenderer() );
		return comboBox;
	}

	static List< String > getColorSchemeOptions( ProjectModel projectModel, ColoringModel coloringModel )
	{
		List< String > tagSets = projectModel.getModel().getTagSetModel().getTagSetStructure().getTagSets().stream()
				.map( TagSetStructure.TagSet::getName ).collect( Collectors.toList() );
		List< String > featureColorModes =
				GraphToCsvUtils.getValidFeatureColorModes( coloringModel ).stream().map( FeatureColorMode::getName )
						.collect( Collectors.toList() );
		List< String > options = new ArrayList<>();
		options.add( DEFAULT_OPTION );
		options.add( SEPARATOR );
		options.add( TAGS_HEADER );
		options.addAll( tagSets );
		options.add( SEPARATOR );
		options.add( FEATURE_COLOR_MODES_HEADER );
		options.addAll( featureColorModes );
		return options;
	}

	private static class ColorSchemeOptionsRenderer implements ListCellRenderer< String >
	{

		private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		@Override
		public Component getListCellRendererComponent( JList< ? extends String > list, String value, int index, boolean isSelected, boolean cellHasFocus )
		{
			switch ( value )
			{
			case TAGS_HEADER:
			case FEATURE_COLOR_MODES_HEADER:
				JLabel label = ( JLabel ) defaultRenderer.getListCellRendererComponent( list, value, index, false, cellHasFocus );
				label.setFont( label.getFont().deriveFont( Font.BOLD ) );
				return label;
			case SEPARATOR:
				return new JSeparator( JSeparator.HORIZONTAL );
			default:
				JLabel renderer = ( JLabel ) defaultRenderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
				renderer.setFont( renderer.getFont().deriveFont( Font.PLAIN ) );
				return renderer;
			}
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
