/*
 * Copyright 2016 Malte Finsterwalder
 * Copyright [2018] Pablo Nicolas Diaz Bilotto [https://github.com/PabloNicolasDiaz/]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.redsix.pdfcompare.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

import de.redsix.pdfcompare.CompareResultWithExpectedAndActual;
import de.redsix.pdfcompare.PdfComparator;
import lombok.val;

public class Display {

	private ViewModel viewModel;

	public void init() {
		viewModel = new ViewModel(new CompareResultWithExpectedAndActual());

		val frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		final BorderLayout borderLayout = new BorderLayout();
		frame.setLayout(borderLayout);
		frame.setMinimumSize(new Dimension(400, 200));
		final Rectangle screenBounds = getDefaultScreenBounds();
		frame.setSize(Math.min(screenBounds.width, 1700), Math.min(screenBounds.height, 1000));
		frame.setLocation(screenBounds.x, screenBounds.y);

		val toolBar = new JToolBar();
		toolBar.setRollover(true);
		toolBar.setFloatable(false);
		frame.add(toolBar, BorderLayout.PAGE_START);

		val leftPanel = new ImagePanel(viewModel.getLeftImage());
		val resultPanel = new ImagePanel(viewModel.getDiffImage());

		val expectedScrollPane = new JScrollPane(leftPanel);
		expectedScrollPane.setMinimumSize(new Dimension(200, 200));
		val actualScrollPane = new JScrollPane(resultPanel);
		actualScrollPane.setMinimumSize(new Dimension(200, 200));
		actualScrollPane.getViewport().addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(final ComponentEvent e) {
				resultPanel.setViewSize(e.getComponent().getSize());
				super.componentResized(e);
			}
		});

		expectedScrollPane.getVerticalScrollBar().setModel(actualScrollPane.getVerticalScrollBar().getModel());
		expectedScrollPane.getHorizontalScrollBar().setModel(actualScrollPane.getHorizontalScrollBar().getModel());
		expectedScrollPane.getViewport().addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(final ComponentEvent e) {
				leftPanel.setViewSize(e.getComponent().getSize());
				super.componentResized(e);
			}
		});

		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, expectedScrollPane, actualScrollPane);
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerLocation(0.5);
		splitPane.setOneTouchExpandable(true);
		frame.add(splitPane, BorderLayout.CENTER);

		final JToggleButton expectedButton = new JToggleButton("Expected");

		addToolBarButton(toolBar, "Open...", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JFileChooser fileChooser = new JFileChooser();
				try {
					if (fileChooser.showDialog(frame, "Open expected PDF") == JFileChooser.APPROVE_OPTION) {
						val expectedFile = fileChooser.getSelectedFile();
						val passwordForExpectedFile = askForPassword(expectedFile);
						if (fileChooser.showDialog(frame, "Open actual PDF") == JFileChooser.APPROVE_OPTION) {
							val actualFile = fileChooser.getSelectedFile();
							val passwordForActualFile = askForPassword(actualFile);
							try {
								frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
								val compareResult = (CompareResultWithExpectedAndActual) new PdfComparator<>(
										expectedFile, actualFile, new CompareResultWithExpectedAndActual())
												.withExpectedPassword(
														String.valueOf(passwordForExpectedFile.getPassword()))
												.withActualPassword(String.valueOf(passwordForActualFile.getPassword()))
												.compare();
								viewModel = new ViewModel(compareResult);
								leftPanel.setImage(viewModel.getLeftImage());
								resultPanel.setImage(viewModel.getDiffImage());
								if (compareResult.isEqual()) {
									JOptionPane.showMessageDialog(frame, "The compared documents are identical.");
								}
								frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
								expectedButton.setSelected(true);
							} catch (IOException ex) {
								DisplayExceptionDialog(frame, ex);
							}
						}
					}
				} catch (IOException ex) {
					DisplayExceptionDialog(frame, ex);
				}

			}
		});

		toolBar.addSeparator();

		addToolBarButton(toolBar, "Page -", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (viewModel.decreasePage()) {
					leftPanel.setImage(viewModel.getLeftImage());
					resultPanel.setImage(viewModel.getDiffImage());
				}
			}
		});

		addToolBarButton(toolBar, "Page +", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (viewModel.increasePage()) {
					leftPanel.setImage(viewModel.getLeftImage());
					resultPanel.setImage(viewModel.getDiffImage());
				}
			}
		});

		toolBar.addSeparator();

		val pageZoomButton = new JToggleButton("Zoom Page");
		pageZoomButton.setSelected(true);
		pageZoomButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				leftPanel.zoomPage();
				resultPanel.zoomPage();
			}
		});

		addToolBarButton(toolBar, "Zoom -", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				pageZoomButton.setSelected(false);
				leftPanel.decreaseZoom();
				resultPanel.decreaseZoom();
			}
		});

		addToolBarButton(toolBar, "Zoom +", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				pageZoomButton.setSelected(false);
				leftPanel.increaseZoom();
				resultPanel.increaseZoom();
			}
		});

		toolBar.add(pageZoomButton);

		addToolBarButton(toolBar, "Zoom 100%", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				pageZoomButton.setSelected(false);
				leftPanel.zoom100();
				resultPanel.zoom100();
			}
		});

		toolBar.addSeparator();

		addToolBarButton(toolBar, "Center Split", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				splitPane.setDividerLocation(0.5);
				splitPane.revalidate();
			}
		});

		toolBar.addSeparator();

		val buttonGroup = new ButtonGroup();
		expectedButton.setSelected(true);
		expectedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				viewModel.showExpected();
				leftPanel.setImage(viewModel.getLeftImage());
			}
		});
		toolBar.add(expectedButton);
		buttonGroup.add(expectedButton);

		val actualButton = new JToggleButton("Actual");
		actualButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				viewModel.showActual();
				leftPanel.setImage(viewModel.getLeftImage());
			}
		});
		toolBar.add(actualButton);
		buttonGroup.add(actualButton);

		frame.setVisible(true);
	}

	private static void DisplayExceptionDialog(final JFrame frame, final IOException ex) {
		val stringWriter = new StringWriter();
		ex.printStackTrace(new PrintWriter(stringWriter));
		val textArea = new JTextArea(
				"Es ist ein unerwarteter Fehler aufgetreten: " + ex.getMessage() + "\n\n" + stringWriter);
		val scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(900, 700));
		JOptionPane.showMessageDialog(frame, scrollPane);
	}

	private static void addToolBarButton(final JToolBar toolBar, final String label,
			final ActionListener actionListener) {
		val button = new JButton(label);
		button.addActionListener(actionListener);
		toolBar.add(button);
	}

	private static Rectangle getDefaultScreenBounds() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
				.getBounds();
	}

	private static JPasswordField askForPassword(final File file) throws IOException {
		val passwordForFile = new JPasswordField(10);
		if (isInvalidPassword(file, "")) {
			val label = new JLabel("Enter password: ");
			label.setLabelFor(passwordForFile);

			val textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
			textPane.add(label);
			textPane.add(passwordForFile);

			JOptionPane.showMessageDialog(null, textPane, "PDF is encrypted", JOptionPane.INFORMATION_MESSAGE);

			label.setText("Password was invalid. Enter password: ");
			while (isInvalidPassword(file, String.valueOf(passwordForFile.getPassword()))) {
				passwordForFile.setText("");
				JOptionPane.showMessageDialog(null, textPane, "PDF is encrypted", JOptionPane.ERROR_MESSAGE);
			}
		}
		return passwordForFile;
	}

	private static boolean isInvalidPassword(final File file, final String password) throws IOException {
		try {
			PDDocument.load(file, password).close();
		} catch (InvalidPasswordException e) {
			return true;
		}
		return false;
	}
}
