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

	public static void show( Component parentComponent, String title, String message, Throwable exception ) {

		final JTextArea textArea = initTextArea( exception );
		final JScrollPane scrollPane = initScrollPane( textArea );

		JLabel showDetailsLabel = new JLabel("Show Details ...");
		showDetailsLabel.setForeground(Color.BLUE);
		showDetailsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

		JPanel panel = new JPanel(new MigLayout("insets dialog", "[grow]", "[][][grow]"));
		panel.add(new JLabel(message), "wrap");
		panel.add( showDetailsLabel, "wrap");
		panel.add(scrollPane, "grow");

		JOptionPane pane = new JOptionPane(panel, JOptionPane.ERROR_MESSAGE);
		JDialog dialog = pane.createDialog(parentComponent, title);

		showDetailsLabel.addMouseListener( new MouseAdapter() {
			public void mouseClicked( MouseEvent e) {
				boolean visible = !scrollPane.isVisible();
				scrollPane.setVisible( visible );
				scrollPane.setPreferredSize(visible ?
						new Dimension(400, 200) :
						new Dimension(0, 0 ));
				dialog.pack();
			};
		});

		dialog.setResizable(true);
		dialog.setVisible(true);
		dialog.dispose();
	}

	private static JScrollPane initScrollPane( JTextArea textArea )
	{
		final JScrollPane scrollPane = new JScrollPane( textArea );
		scrollPane.setPreferredSize(new Dimension(0, 0));
		scrollPane.setVisible(false);
		return scrollPane;
	}

	private static JTextArea initTextArea( Throwable throwable )
	{
		JTextArea textArea = new JTextArea();
		textArea.setForeground( Color.RED);
		textArea.setEditable( false );
		textArea.setText( asString( throwable ) );
		return textArea;
	}

	private static String asString( Throwable throwable )
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		throwable.printStackTrace(new PrintStream(out));
		return out.toString();
	}

}
