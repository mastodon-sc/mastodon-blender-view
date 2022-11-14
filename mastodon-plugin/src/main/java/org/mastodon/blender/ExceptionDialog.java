package org.mastodon.blender;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ExceptionDialog
{

	public static void showOkDialog( Component parentComponent, String title, String message, Throwable exception ) {
		JPanel panel = initMessagePanel( message, exception );
		JOptionPane pane = new JOptionPane(panel, JOptionPane.ERROR_MESSAGE);
		JDialog dialog = pane.createDialog(parentComponent, title);
		dialog.setResizable(true);
		dialog.setVisible(true);
		dialog.dispose();
	}

	public static boolean showOkCancelDialog( Component parentComponent, String title, String message, Throwable exception, String okText, String cancelText ) {
		JPanel panel = initMessagePanel( message, exception );
		JOptionPane pane = new JOptionPane(panel, JOptionPane.ERROR_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new Object[]{ okText, cancelText });
		JDialog dialog = pane.createDialog(parentComponent, title);
		dialog.setResizable(true);
		dialog.setVisible(true);
		dialog.dispose();
		return okText == pane.getValue();
	}

	private static JPanel initMessagePanel( String message, Throwable exception )
	{
		final JTextArea textArea = initTextArea( exception );
		final JScrollPane scrollPane = initScrollPane( textArea );
		final JLabel showDetailsLabel = initShowDetailsLabel( scrollPane );
		JPanel panel = new JPanel(new MigLayout("insets dialog", "[grow]", "[][][grow]"));
		panel.add(new JLabel( message ), "wrap");
		panel.add( showDetailsLabel, "wrap");
		panel.add(scrollPane, "grow");
		return panel;
	}

	private static JTextArea initTextArea( Throwable throwable )
	{
		JTextArea textArea = new JTextArea();
		textArea.setForeground( Color.RED);
		textArea.setEditable( false );
		textArea.setText( asString( throwable ) );
		return textArea;
	}

	private static JScrollPane initScrollPane( JTextArea textArea )
	{
		final JScrollPane scrollPane = new JScrollPane( textArea );
		scrollPane.setPreferredSize(new Dimension(0, 0));
		scrollPane.setVisible(false);
		return scrollPane;
	}

	private static JLabel initShowDetailsLabel( JScrollPane scrollPane )
	{
		JLabel showDetailsLabel = new JLabel("Show Details ...");
		showDetailsLabel.setForeground(Color.BLUE);
		showDetailsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		showDetailsLabel.addMouseListener( new MouseAdapter() {
			public void mouseClicked( MouseEvent e) {
				boolean visible = !scrollPane.isVisible();
				scrollPane.setVisible( visible );
				scrollPane.setPreferredSize(visible ?
						new Dimension(400, 200) :
						new Dimension(0, 0 ));
				SwingUtilities.getWindowAncestor( e.getComponent() ).pack();
			}
		});
		return showDetailsLabel;
	}

	private static String asString( Throwable throwable )
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		throwable.printStackTrace(new PrintStream(out));
		return out.toString();
	}

}
