package com.marajy.mp3renamer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.Player;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

//import javax.sound.sampled.AudioFormat;

public class Main {

	static Text artisteText;
	static Text titreText;
	static Text newFileName;
	static List list;
	static Text fileName;
	static Text folderPath;
	static Text rateText;
	static Text sizeText;
	static Text durationText;
	static Process p;
	static Thread playerThread;
	private static Player player;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		

		// ============================================
		// GUI
		// ============================================

		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("Mp3 Renamer");

		final Image imageSave = new Image(display, Main.class.getClassLoader()
				.getResourceAsStream("document-save.png"));
		final Image imageExit = new Image(display, Main.class.getClassLoader()
				.getResourceAsStream("application_exit.png"));
		final Image imageFind = new Image(display, Main.class.getClassLoader()
				.getResourceAsStream("deskbar-applet.png"));
		final Image imageMusic = new Image(display, Main.class.getClassLoader()
				.getResourceAsStream("music.png"));
		final Image deleteFile = new Image(display, Main.class.getClassLoader()
				.getResourceAsStream("edit-delete.png"));
		final Image clearList = new Image(display, Main.class.getClassLoader()
				.getResourceAsStream("edit-clear.png"));
		final Image refreshList = new Image(display, Main.class
				.getClassLoader().getResourceAsStream("repeat.png"));
		final Image editCopy = new Image(display, Main.class.getClassLoader()
				.getResourceAsStream("edit-copy.png"));
		// final Image aboutImage = new Image(display,
		// "ressources/icones/about.png");
		final Image mediaStartImage = new Image(display, Main.class
				.getClassLoader().getResourceAsStream("media-start.png"));
		final Image mediaStopImage = new Image(display, Main.class
				.getClassLoader().getResourceAsStream("media-stop.png"));

		shell.setImage(imageMusic);
		Label labelFolder = new Label(shell, SWT.WRAP);
		labelFolder.setText("Folder to explore: ");
		folderPath = new Text(shell, SWT.WRAP);
		folderPath.setText("/home/freeman/Musique/");
		Button scanFolderButton = new Button(shell, SWT.PUSH);
		scanFolderButton.setImage(imageFind);

		list = new List(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		Label label2 = new Label(shell, SWT.WRAP);
		label2.setText("File name");

		fileName = new Text(shell, SWT.WRAP);
		fileName.setEditable(false);

		Label label3 = new Label(shell, SWT.WRAP);
		label3.setText("Artist");
		artisteText = new Text(shell, SWT.WRAP);
		artisteText.setEditable(false);

		Label label4 = new Label(shell, SWT.WRAP);
		label4.setText("Song title");
		titreText = new Text(shell, SWT.WRAP);
		titreText.setEditable(false);

		Label label5 = new Label(shell, SWT.WRAP);
		label5.setText("New File Name");
		newFileName = new Text(shell, SWT.WRAP);

		final int insetX = 4, insetY = 4;
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = insetX;
		formLayout.marginHeight = insetY;
		shell.setLayout(formLayout);

		final FormData labelData = new FormData(100, SWT.DEFAULT);
		labelData.left = new FormAttachment(0, 0);
		labelData.width = 120;
		labelFolder.setLayoutData(labelData);

		final FormData folderPathData = new FormData(100, SWT.DEFAULT);
		folderPathData.left = new FormAttachment(labelFolder, 10);
		folderPathData.right = new FormAttachment(scanFolderButton, -10);
		folderPath.setLayoutData(folderPathData);

		final FormData scanFolderButtonData = new FormData(100, SWT.DEFAULT);
		scanFolderButtonData.right = new FormAttachment(100, 0);
		scanFolderButton.setLayoutData(scanFolderButtonData);
		scanFolderButtonData.width = 70;
		// Copy button
		final Button copyButton = new Button(shell, SWT.PUSH);
		copyButton.setImage(editCopy);
		FormData copyButtonData = new FormData();
		copyButtonData.right = new FormAttachment(60, 0);
		copyButtonData.bottom = new FormAttachment(100, -5);
		copyButtonData.width = 70;
		copyButton.setLayoutData(copyButtonData);

		// About button
		final Button mediaButton = new Button(shell, SWT.PUSH);
		mediaButton.setImage(mediaStartImage);
		FormData mediaButtonData = new FormData();
		mediaButtonData.right = new FormAttachment(70, 0);
		mediaButtonData.bottom = new FormAttachment(100, -5);
		mediaButtonData.width = 70;
		mediaButton.setLayoutData(mediaButtonData);
		// Exit button
		final Button exitButton = new Button(shell, SWT.PUSH);
		exitButton.setImage(imageExit);
		FormData exitButtonData = new FormData();
		exitButtonData.right = new FormAttachment(50, 0);
		exitButtonData.bottom = new FormAttachment(100, -5);
		exitButtonData.width = 70;
		exitButton.setLayoutData(exitButtonData);

		// Save button
		final Button saveButton = new Button(shell, SWT.PUSH);
		saveButton.setImage(imageSave);
		FormData saveButtonData = new FormData();
		saveButtonData.right = new FormAttachment(40, 0);
		saveButtonData.bottom = new FormAttachment(100, -5);
		saveButtonData.width = 70;
		saveButton.setLayoutData(saveButtonData);

		// Clear button
		final Button clearListButton = new Button(shell, SWT.PUSH);
		clearListButton.setImage(clearList);
		FormData clearListButtonData = new FormData();
		clearListButtonData.right = new FormAttachment(10, 0);
		clearListButtonData.bottom = new FormAttachment(100, -5);
		clearListButtonData.width = 70;
		clearListButton.setLayoutData(clearListButtonData);

		// Refresh button
		final Button refreshButton = new Button(shell, SWT.PUSH);
		refreshButton.setImage(refreshList);
		FormData refreshButtonData = new FormData();
		refreshButtonData.right = new FormAttachment(20, 0);
		refreshButtonData.bottom = new FormAttachment(100, -5);
		refreshButtonData.width = 70;
		refreshButton.setLayoutData(refreshButtonData);

		// Delete button
		final Button deleteButton = new Button(shell, SWT.PUSH);
		deleteButton.setImage(deleteFile);
		FormData deleteButtonData = new FormData();
		deleteButtonData.right = new FormAttachment(30, 0);
		deleteButtonData.bottom = new FormAttachment(100, -5);
		deleteButtonData.width = 70;
		deleteButton.setLayoutData(deleteButtonData);

		// List component
		final FormData listData = new FormData();
		listData.left = new FormAttachment(0, 0);
		listData.right = new FormAttachment(100, 0);
		listData.top = new FormAttachment(scanFolderButton, 5);
		listData.bottom = new FormAttachment(fileName, -5);
		list.setLayoutData(listData);

		FormData label2Data = new FormData();
		label2Data.bottom = new FormAttachment(label3, -5);
		label2Data.left = new FormAttachment(0, 5);
		label2.setLayoutData(label2Data);

		FormData textData = new FormData();
		textData.bottom = new FormAttachment(label3, -5);
		textData.right = new FormAttachment(100, 0);
		textData.left = new FormAttachment(0, 100);
		fileName.setLayoutData(textData);

		FormData label3Data = new FormData();
		label3Data.bottom = new FormAttachment(label4, -5);
		label3Data.left = new FormAttachment(0, 5);
		label3.setLayoutData(label3Data);

		FormData text2Data = new FormData();
		text2Data.bottom = new FormAttachment(label4, -5);
		text2Data.right = new FormAttachment(100, 0);
		text2Data.left = new FormAttachment(0, 100);
		artisteText.setLayoutData(text2Data);

		FormData label4Data = new FormData();
		label4Data.bottom = new FormAttachment(label5, -5);
		label4Data.left = new FormAttachment(0, 5);
		label4.setLayoutData(label4Data);

		FormData text3Data = new FormData();
		text3Data.bottom = new FormAttachment(label5, -5);
		text3Data.right = new FormAttachment(100, 0);
		text3Data.left = new FormAttachment(0, 100);
		titreText.setLayoutData(text3Data);

		// Rate label
		Label rateLabel = new Label(shell, SWT.WRAP);
		rateLabel.setText("File rate");
		FormData rateLabelData = new FormData();
		rateLabelData.bottom = new FormAttachment(exitButton, -5);
		rateLabelData.left = new FormAttachment(0, 5);
		rateLabel.setLayoutData(rateLabelData);

		// Rate text
		rateText = new Text(shell, SWT.WRAP);
		rateText.setEditable(false);
		FormData rateTextData = new FormData();
		rateTextData.bottom = new FormAttachment(exitButton, -5);
		rateTextData.left = new FormAttachment(0, 100);
		rateText.setLayoutData(rateTextData);

		// Size Label
		Label sizeLabel = new Label(shell, SWT.WRAP);
		sizeLabel.setText("File size");
		FormData sizeLabelData = new FormData();
		sizeLabelData.bottom = new FormAttachment(exitButton, -5);
		sizeLabelData.left = new FormAttachment(rateText, 100);
		sizeLabel.setLayoutData(sizeLabelData);

		// Size text
		sizeText = new Text(shell, SWT.WRAP);
		sizeText.setEditable(false);
		FormData sizeTextData = new FormData();
		sizeTextData.bottom = new FormAttachment(exitButton, -5);
		sizeTextData.left = new FormAttachment(sizeLabel, 5);
		sizeTextData.width = 100;
		sizeText.setLayoutData(sizeTextData);

		// Duration Label
		Label durationLabel = new Label(shell, SWT.WRAP);
		durationLabel.setText("File duration");
		FormData durationLabellData = new FormData();
		durationLabellData.bottom = new FormAttachment(exitButton, -5);
		durationLabellData.left = new FormAttachment(sizeText, 100);
		durationLabel.setLayoutData(durationLabellData);

		// Duration text
		durationText = new Text(shell, SWT.WRAP);
		durationText.setEditable(false);
		FormData durationTextData = new FormData();
		durationTextData.bottom = new FormAttachment(exitButton, -5);
		durationTextData.left = new FormAttachment(durationLabel, 5);
		durationTextData.width = 100;
		durationText.setLayoutData(durationTextData);

		FormData label5Data = new FormData();
		label5Data.bottom = new FormAttachment(rateLabel, -5);
		label5Data.left = new FormAttachment(0, 5);
		label5.setLayoutData(label5Data);

		FormData text4Data = new FormData();
		text4Data.bottom = new FormAttachment(rateLabel, -5);
		text4Data.right = new FormAttachment(100, 0);
		text4Data.left = new FormAttachment(0, 100);
		newFileName.setLayoutData(text4Data);
		// ==================================================
		// Actions
		// ==================================================

		scanFolderButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				DirectoryDialog dlg = new DirectoryDialog(shell);
				dlg.setFilterPath(folderPath.getText());
				dlg.setMessage("Select a directory");
				String folder = dlg.open();
				if (folder != null && !"".equals(folder)) {
					folderPath.setText(folder);
					scan();
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				fileName.setText("Widget default selected");

			}
		});

		exitButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				display.dispose();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		refreshButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				scan();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		clearListButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				reset();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		deleteButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				MessageBox msg = new MessageBox(shell, SWT.ICON_WARNING
						| SWT.CANCEL | SWT.NO | SWT.YES);
				msg.setMessage("Are you sure you want to delete this file: "
						+ fileName.getText() + " ?");
				msg.setText("Delete a file");
				int answer = msg.open();
				// System.out.println(answer);
				if (SWT.YES == answer) {
					int index = list.getSelectionIndex();
					File file = new File(list.getSelection()[0]);
					if (file.exists()) {
						System.out.println("delete");
						file.delete();
						scan();
						list.setSelection(index);
					}

				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		list.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				resetButList();
				try {
					File file = new File(list.getSelection()[0]);
					fileName.setText(file.getName());
					AudioFile mp3 = AudioFileIO.read(file);
					Tag tag = mp3.getTag();
					String artiste = tag.getFirst(FieldKey.ARTIST);
					String titre = tag.getFirst(FieldKey.TITLE);
					rateText.setText(mp3.getAudioHeader().getBitRate() + "Kb/s");
					DecimalFormat df = new DecimalFormat("###,###,###,###.##");
					sizeText.setText(df.format(file.length() / 1024) + " Ko");
					DecimalFormat df2 = new DecimalFormat("00");
					durationText.setText(df2.format(mp3.getAudioHeader()
							.getTrackLength() / 3600)
							+ ":"
							+ df2.format(mp3.getAudioHeader().getTrackLength() / 60)
							+ ":"
							+ df2.format(mp3.getAudioHeader().getTrackLength() % 60));

					artisteText.setText(artiste);
					titreText.setText(titre);
					if (artiste.length() > 1) {
						artiste = artiste.replace('_', ' ');
						artiste = artiste.replace('/', ' ');
						artiste = artiste.replace('\\', ' ');
						artiste = artiste.replace('.', ' ');
						artiste = artiste.substring(0, 1).toUpperCase()
								+ artiste.substring(1);
					}
					if (titre.length() > 1) {
						titre = titre.replace('_', ' ');
						titre = titre.replace('/', ' ');
						titre = titre.replace('\\', ' ');
						titre = titre.replace('.', ' ');
						titre = titre.substring(0, 1).toUpperCase()
								+ titre.substring(1);
					}
					newFileName.setText(artiste + " - " + titre + ".mp3");

				} catch (CannotReadException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TagException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReadOnlyFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidAudioFrameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (KeyNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (newFileName.getText().length() == 0)
						newFileName.setText(fileName.getText());
				}

			}
		});

		saveButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				File file = new File(list.getSelection()[0]);
				int index = list.getSelectionIndex();
				File fileDest = new File(file.getParentFile() + "/"
						+ newFileName.getText());
				if (fileDest.exists()) {
					MessageBox msg = new MessageBox(shell);
					msg.setMessage("Il existe déjà un fichier avec ce nom dans le repertoire!!!");
					msg.open();
				} else {
					file.renameTo(fileDest);
					System.out.println(file.getParentFile() + "/"
							+ newFileName.getText());
					scan();
					list.select(index);
					list.notifyListeners(SWT.Selection, new Event());
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		copyButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				newFileName.setText(fileName.getText());

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		mediaButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {

				/*
				 * try { if (list != null && list.getSelection() != null &&
				 * list.getSelection().length > 0) { if (p == null) { String[]
				 * args = new String[2]; args[0] = "vlc"; args[1] = "file://" +
				 * list.getSelection()[0].toString() + "";
				 * System.out.println(args[0] + args[1]); p =
				 * Runtime.getRuntime().exec(args);
				 * System.out.println(p.getOutputStream().toString());
				 * mediaButton.setImage(mediaStopImage); } else { String[] args
				 * = new String[2]; System.out.println(args[0] + args[1]);
				 * p.destroy(); p = null; mediaButton.setImage(mediaStartImage);
				 * } } else { if (p == null) { String[] args = new String[2];
				 * args[0] = "vlc"; args[1] = folderPath.getText(); //+
				 * list.getSelection()[0].toString() + "";
				 * System.out.println(args[0] + args[1]); p =
				 * Runtime.getRuntime().exec(args);
				 * System.out.println(p.getOutputStream().toString());
				 * mediaButton.setImage(mediaStopImage); } else { String[] args
				 * = new String[2]; System.out.println(args[0] + args[1]);
				 * p.destroy(); p = null; mediaButton.setImage(mediaStartImage);
				 * } } } catch (IOException e) { // TODO Auto-generated catch
				 * block e.printStackTrace(); }
				 */
				/*
				 * try { // Creer un nouveau lecteur et ajouter un ?couteur. if
				 * (lecteur != null) lecteur.close(); lecteur =
				 * Manager.createPlayer(new MediaLocator("file:/" +
				 * list.getSelection()[0].toString())); //
				 * lecteur.addControllerListener( new EventHandler() );
				 * lecteur.addControllerListener(new ControllerListener() {
				 * public void controllerUpdate(ControllerEvent event) { if
				 * (event instanceof EndOfMediaEvent) { lecteur.stop();
				 * lecteur.close(); } } }); lecteur.realize();
				 * System.out.println("file:/" +
				 * list.getSelection()[0].toString() + " "+ lecteur.getRate());
				 * lecteur.start(); // D?marrage du lecteur. } catch (Exception
				 * e) { System.out.println("Fichier ou emplacement invalide" +
				 * "Chargement du fichier" + " erron?"); e.printStackTrace(); }
				 */

				getTask(list.getSelection()[0].toString()).start();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		// shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	static void addTree(File file, ArrayList<String> all) {
		File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {
				if (child.isFile()
						&& child.getName().toLowerCase().endsWith(".mp3")) {
					all.add(child.getAbsolutePath());
				}
				addTree(child, all);
			}
		}
	}

	static void reset() {
		resetButList();
		list.removeAll();
		list.redraw();
	}

	static void resetButList() {
		artisteText.setText("");
		titreText.setText("");
		newFileName.setText("");
		fileName.setText("");
		rateText.setText("");
		sizeText.setText("");
		durationText.setText("");

		durationText.redraw();
	}

	static void scan() {
		reset();
		ArrayList<String> aList = new ArrayList<String>();
		addTree(new File(folderPath.getText()), aList);
		Collections.sort(aList);
		list.setItems(aList.toArray(new String[aList.size()]));
		list.redraw();
	}

	
	public static Thread getTask(String fileName) {
		final String fFileName = fileName;
		if (player != null) {
			player.close();
		}
		if (playerThread != null ) {
			playerThread.interrupt();
		}

		playerThread = new Thread() {
			public void run() {
				try {

					System.out.println("playing " + fFileName + "...");
					InputStream in = new FileInputStream(new File(fFileName));

					AudioDevice dev = FactoryRegistry.systemRegistry()
							.createAudioDevice();
					player = new Player(in, dev);
					player.play();
				} catch (IOException ex) {
					System.out.println("Problem playing file " + fFileName);
					ex.printStackTrace();
				} catch (Exception ex) {
					System.out.println("Problem playing file " + fFileName);
					ex.printStackTrace();
				}
			}
		};

		return playerThread;

	}
}
