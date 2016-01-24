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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
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
	static List listLeft;
	static Text fileName;
	static Text folderPath;
	static Text folderPathLeft;
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
		final Image imageSave = new Image(display, Main.class.getClassLoader().getResourceAsStream("save.png"));
		final Image imageExit = new Image(display, Main.class.getClassLoader().getResourceAsStream("application_exit.png"));
		final Image imageFind = new Image(display, Main.class.getClassLoader().getResourceAsStream("folder.png"));
		final Image imageMusic = new Image(display, Main.class.getClassLoader().getResourceAsStream("music.png"));
		final Image deleteFile = new Image(display, Main.class.getClassLoader().getResourceAsStream("edit-delete.png"));
		final Image clearList = new Image(display, Main.class.getClassLoader().getResourceAsStream("edit-clear.png"));
		final Image refreshList = new Image(display, Main.class.getClassLoader().getResourceAsStream("repeat.png"));
		final Image editCopy = new Image(display, Main.class.getClassLoader().getResourceAsStream("edit-copy.png"));
		final Image aboutImage = new Image(display, "ressources/icones/about.png");
		final Image mediaStartImage = new Image(display, Main.class.getClassLoader().getResourceAsStream("media-start.png"));
		final Image mediaStopImage = new Image(display, Main.class.getClassLoader().getResourceAsStream("media-stop.png"));
		final Image mediaPlayAllImage = new Image(display, Main.class.getClassLoader().getResourceAsStream("media-forward.png"));

		final Shell shell = new Shell(display);
		Menu appMenuBar = display.getMenuBar();
		if (appMenuBar == null) {
			appMenuBar = new Menu(shell, SWT.BAR);
			shell.setMenuBar(appMenuBar);
		}
		MenuItem file = new MenuItem(appMenuBar, SWT.CASCADE);
		file.setText("File");
		Menu dropdown = new Menu(appMenuBar);
		file.setMenu(dropdown);
		MenuItem exit = new MenuItem(dropdown, SWT.PUSH);
		exit.setText("Exit");
		exit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				display.dispose();
			};
		});

		Image image = new Image(display, 20, 20);
		Color color = display.getSystemColor(SWT.COLOR_BLUE);
		GC gc = new GC(image);
		gc.setBackground(color);
		gc.fillRectangle(image.getBounds());
		gc.dispose();

		Image disabledImage = new Image(display, 20, 20);
		color = display.getSystemColor(SWT.COLOR_GREEN);
		gc = new GC(disabledImage);
		gc.setBackground(color);
		gc.fillRectangle(disabledImage.getBounds());
		gc.dispose();

		Image hotImage = new Image(display, 20, 20);
		color = display.getSystemColor(SWT.COLOR_RED);
		gc = new GC(hotImage);
		gc.setBackground(color);
		gc.fillRectangle(hotImage.getBounds());
		gc.dispose();

		ToolBar bar = new ToolBar(shell, SWT.BORDER | SWT.FLAT);
		Rectangle clientArea = shell.getClientArea();
		bar.setBounds(clientArea.x, clientArea.y, 200, 32);

		ToolItem item = new ToolItem(bar, 0);
		item.setImage(imageSave);
		item.setDisabledImage(disabledImage);
		item.setHotImage(hotImage);

		FormLayout formLayout = new FormLayout();
		final int insetX = 4, insetY = 4;
		formLayout.marginWidth = insetX;
		formLayout.marginHeight = insetY;
		shell.setLayout(formLayout);
		// shell.setFullScreen(true);
		shell.setMaximized(true);

		ToolBar toolBar = new ToolBar(shell, SWT.FLAT);
		// toolBar.setLayoutData(formLayout);

		shell.setText("Mp3 Renamer");

		shell.setImage(imageMusic);
		// Label labelFolder = new Label(shell, SWT.WRAP);
		// labelFolder.setText("Folder to explore: ");
		folderPath = new Text(shell, SWT.WRAP);
		folderPathLeft = new Text(shell, SWT.WRAP);
		folderPath.setText(System.getProperty("user.home"));
		folderPathLeft.setText(System.getProperty("user.home"));
		folderPath.setEditable(false);
		folderPathLeft.setEditable(false);

		Button scanFolderButton = new Button(shell, SWT.PUSH);
		scanFolderButton.setImage(imageFind);
		Button scanFolderLeftButton = new Button(shell, SWT.PUSH);
		scanFolderLeftButton.setImage(imageFind);

		list = new List(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listLeft = new List(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

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

		final FormData folderPathLeftData = new FormData(100, SWT.DEFAULT);
		folderPathLeftData.top = new FormAttachment(bar, 5);
		folderPathLeftData.left = new FormAttachment(scanFolderLeftButton, 5);
		folderPathLeftData.right = new FormAttachment(49, 0);
		folderPathLeftData.height = 22;
		folderPathLeft.setLayoutData(folderPathLeftData);

		final FormData folderPathData = new FormData(100, SWT.DEFAULT);
		folderPathData.top = new FormAttachment(bar, 5);
		folderPathData.left = new FormAttachment(scanFolderButton, 5);
		folderPathData.right = new FormAttachment(100, 0);
		folderPathData.height = 22;
		folderPath.setLayoutData(folderPathData);

		final FormData scanFolderButtonData = new FormData(100, SWT.DEFAULT);
		scanFolderButtonData.top = new FormAttachment(bar, 5);
		scanFolderButtonData.right = new FormAttachment(50, 32);
		scanFolderButtonData.width = 32;
		scanFolderButton.setLayoutData(scanFolderButtonData);

		final FormData scanFolderLeftButtonData = new FormData(100, SWT.DEFAULT);
		scanFolderLeftButtonData.top = new FormAttachment(bar, 5);
		scanFolderLeftButtonData.right = new FormAttachment(0, 32);
		scanFolderLeftButtonData.width = 32;
		scanFolderLeftButton.setLayoutData(scanFolderLeftButtonData);

		// Exit button
		/*
		 * final Button exitButton = new Button(shell, SWT.PUSH);
		 * exitButton.setImage(imageExit); FormData exitButtonData = new
		 * FormData(); exitButtonData.right = new FormAttachment(0, 50);
		 * exitButtonData.bottom = new FormAttachment(100, -5);
		 * exitButtonData.width = 70; exitButton.setLayoutData(exitButtonData);
		 */

		// Clear button
		final Button clearListButton = new Button(shell, SWT.PUSH);
		clearListButton.setImage(clearList);
		FormData clearListButtonData = new FormData();
		clearListButtonData.right = new FormAttachment(0, 50);
		clearListButtonData.bottom = new FormAttachment(100, -5);
		clearListButtonData.width = 50;
		clearListButton.setLayoutData(clearListButtonData);

		// Refresh button
		final Button refreshButton = new Button(shell, SWT.PUSH);
		refreshButton.setImage(refreshList);
		FormData refreshButtonData = new FormData();
		refreshButtonData.left = new FormAttachment(clearListButton, 5);
		refreshButtonData.bottom = new FormAttachment(100, -5);
		refreshButtonData.width = 50;
		refreshButton.setLayoutData(refreshButtonData);

		// Delete button
		final Button deleteButton = new Button(shell, SWT.PUSH);
		deleteButton.setImage(deleteFile);
		FormData deleteButtonData = new FormData();
		deleteButtonData.left = new FormAttachment(refreshButton, 5);
		deleteButtonData.bottom = new FormAttachment(100, -5);
		deleteButtonData.width = 50;
		deleteButton.setLayoutData(deleteButtonData);

		// Save button
		final Button saveButton = new Button(shell, SWT.PUSH);
		saveButton.setImage(imageSave);
		FormData saveButtonData = new FormData();
		saveButtonData.left = new FormAttachment(deleteButton, 5);
		saveButtonData.bottom = new FormAttachment(100, -5);
		saveButtonData.width = 50;
		saveButtonData.height = 42;
		saveButton.setLayoutData(saveButtonData);
		saveButton.setToolTipText("Enregistrer le nouveau nom du fichier");

		// Copy button
		final Button copyButton = new Button(shell, SWT.PUSH);
		copyButton.setImage(editCopy);
		FormData copyButtonData = new FormData();
		copyButtonData.left = new FormAttachment(saveButton, 5);
		copyButtonData.bottom = new FormAttachment(100, -5);
		copyButtonData.width = 50;
		copyButton.setLayoutData(copyButtonData);

		// Media start button
		final Button mediaButton = new Button(shell, SWT.PUSH);
		mediaButton.setImage(mediaStartImage);
		FormData mediaButtonData = new FormData();
		mediaButtonData.left = new FormAttachment(copyButton, 5);
		mediaButtonData.bottom = new FormAttachment(100, -5);
		mediaButtonData.width = 50;
		mediaButton.setLayoutData(mediaButtonData);

		// Play All button
		final Button playAllButton = new Button(shell, SWT.PUSH);
		playAllButton.setImage(mediaPlayAllImage);
		FormData playAllButtonData = new FormData();
		playAllButtonData.left = new FormAttachment(mediaButton, 5);
		playAllButtonData.bottom = new FormAttachment(100, -5);
		playAllButtonData.width = 50;
		playAllButton.setLayoutData(playAllButtonData);

		// Stop button
		final Button stopButton = new Button(shell, SWT.PUSH);
		stopButton.setImage(mediaStopImage);
		FormData stopButtonData = new FormData();
		stopButtonData.left = new FormAttachment(playAllButton, 5);
		stopButtonData.bottom = new FormAttachment(100, -5);
		stopButtonData.width = 50;
		stopButton.setLayoutData(stopButtonData);

		// List left component
		final FormData listLeftData = new FormData();
		listLeftData.left = new FormAttachment(0, 0);
		listLeftData.right = new FormAttachment(49, 0);
		listLeftData.top = new FormAttachment(scanFolderButton, 5);
		listLeftData.bottom = new FormAttachment(fileName, -5);
		listLeft.setLayoutData(listLeftData);

		// List component
		final FormData listData = new FormData();
		listData.left = new FormAttachment(50, 0);
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

		// Rate text
		rateText = new Text(shell, SWT.WRAP);
		rateText.setEditable(false);
		FormData rateTextData = new FormData();
		rateTextData.bottom = new FormAttachment(clearListButton, -35);
		rateTextData.left = new FormAttachment(0, 100);
		rateText.setLayoutData(rateTextData);

		// Size Label
		Label sizeLabel = new Label(shell, SWT.WRAP);
		sizeLabel.setText("File size");
		FormData sizeLabelData = new FormData();
		sizeLabelData.bottom = new FormAttachment(clearListButton, -35);
		sizeLabelData.left = new FormAttachment(rateText, 100);
		sizeLabel.setLayoutData(sizeLabelData);

		// Size text
		sizeText = new Text(shell, SWT.WRAP);
		sizeText.setEditable(false);
		FormData sizeTextData = new FormData();
		sizeTextData.bottom = new FormAttachment(clearListButton, -35);
		sizeTextData.left = new FormAttachment(sizeLabel, 5);
		sizeTextData.width = 100;
		sizeText.setLayoutData(sizeTextData);

		// Duration Label
		Label durationLabel = new Label(shell, SWT.WRAP);
		durationLabel.setText("File duration");
		FormData durationLabellData = new FormData();
		durationLabellData.bottom = new FormAttachment(clearListButton, -35);
		durationLabellData.left = new FormAttachment(sizeText, 100);
		durationLabel.setLayoutData(durationLabellData);

		// Duration text
		durationText = new Text(shell, SWT.WRAP);
		durationText.setEditable(false);
		FormData durationTextData = new FormData();
		durationTextData.bottom = new FormAttachment(clearListButton, -35);
		durationTextData.left = new FormAttachment(durationLabel, 5);
		durationTextData.width = 100;
		durationText.setLayoutData(durationTextData);

		// Rate label
		Label rateLabel = new Label(shell, SWT.WRAP);
		rateLabel.setText("File rate");
		FormData rateLabelData = new FormData();
		rateLabelData.bottom = new FormAttachment(clearListButton, -35);
		rateLabelData.left = new FormAttachment(0, 5);
		rateLabel.setLayoutData(rateLabelData);

		FormData label5Data = new FormData();
		label5Data.bottom = new FormAttachment(rateLabel, -5);
		label5Data.left = new FormAttachment(0, 5);
		label5.setLayoutData(label5Data);

		FormData text4Data = new FormData();
		text4Data.bottom = new FormAttachment(rateLabel, -5);
		text4Data.right = new FormAttachment(100, 0);
		text4Data.left = new FormAttachment(0, 100);
		newFileName.setLayoutData(text4Data);

		/*
		 * final Scale pb = new Scale(shell, SWT.WRAP); FormData pbData = new
		 * FormData(); pbData.top = new FormAttachment(rateLabel, 10);
		 * pbData.left = new FormAttachment(0, 0); pbData.right = new
		 * FormAttachment(100, 0); / pbData.width = 100;
		 * pb.setLayoutData(pbData);
		 */
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

		scanFolderLeftButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				DirectoryDialog dlg = new DirectoryDialog(shell);
				dlg.setFilterPath(folderPath.getText());
				dlg.setMessage("Select a directory");
				String folder = dlg.open();
				if (folder != null && !"".equals(folder)) {
					folderPathLeft.setText(folder);
					scanLeft();
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				fileName.setText("Widget default selected");

			}
		});

		/*
		 * exitButton.addSelectionListener(new SelectionListener() {
		 * 
		 * @Override public void widgetSelected(SelectionEvent arg0) {
		 * display.dispose();
		 * 
		 * }
		 * 
		 * @Override public void widgetDefaultSelected(SelectionEvent arg0) { //
		 * TODO Auto-generated method stub
		 * 
		 * } });
		 */

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
				MessageBox msg = new MessageBox(shell, SWT.ICON_WARNING | SWT.CANCEL | SWT.NO | SWT.YES);
				msg.setMessage("Are you sure you want to delete this file: " + fileName.getText() + " ?");
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

					File file = new File(folderPath.getText() + "/" + list.getSelection()[0]);
					fileName.setText(file.getName());
					System.out.println(file.getAbsolutePath());
					AudioFile mp3 = AudioFileIO.read(file);
					Tag tag = mp3.getTag();
					String artiste = tag.getFirst(FieldKey.ARTIST);
					String titre = tag.getFirst(FieldKey.TITLE);
					rateText.setText(mp3.getAudioHeader().getBitRate() + "Kb/s");
					DecimalFormat df = new DecimalFormat("###,###,###,###.##");
					sizeText.setText(df.format(file.length() / 1024) + " Ko");
					DecimalFormat df2 = new DecimalFormat("00");
					durationText.setText(df2.format(mp3.getAudioHeader().getTrackLength() / 3600) + ":" + df2.format(mp3.getAudioHeader().getTrackLength() / 60) + ":" + df2.format(mp3.getAudioHeader().getTrackLength() % 60));

					artisteText.setText(artiste);
					titreText.setText(titre);
					if (artiste.length() > 1) {
						artiste = artiste.replace('_', ' ');
						artiste = artiste.replace('/', ' ');
						artiste = artiste.replace('\\', ' ');
						artiste = artiste.replace('.', ' ');
						artiste = artiste.substring(0, 1).toUpperCase() + artiste.substring(1);
					}
					if (titre.length() > 1) {
						titre = titre.replace('_', ' ');
						titre = titre.replace('/', ' ');
						titre = titre.replace('\\', ' ');
						titre = titre.replace('.', ' ');
						titre = titre.substring(0, 1).toUpperCase() + titre.substring(1);
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
		listLeft.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				resetButList();
				try {

					File file = new File(folderPathLeft.getText() + "/" + listLeft.getSelection()[0]);
					fileName.setText(file.getName());
					System.out.println(file.getAbsolutePath());
					AudioFile mp3 = AudioFileIO.read(file);
					Tag tag = mp3.getTag();
					String artiste = tag.getFirst(FieldKey.ARTIST);
					String titre = tag.getFirst(FieldKey.TITLE);
					rateText.setText(mp3.getAudioHeader().getBitRate() + "Kb/s");
					DecimalFormat df = new DecimalFormat("###,###,###,###.##");
					sizeText.setText(df.format(file.length() / 1024) + " Ko");
					DecimalFormat df2 = new DecimalFormat("00");
					durationText.setText(df2.format(mp3.getAudioHeader().getTrackLength() / 3600) + ":" + df2.format(mp3.getAudioHeader().getTrackLength() / 60) + ":" + df2.format(mp3.getAudioHeader().getTrackLength() % 60));

					artisteText.setText(artiste);
					titreText.setText(titre);
					if (artiste.length() > 1) {
						artiste = artiste.replace('_', ' ');
						artiste = artiste.replace('/', ' ');
						artiste = artiste.replace('\\', ' ');
						artiste = artiste.replace('.', ' ');
						artiste = artiste.substring(0, 1).toUpperCase() + artiste.substring(1);
					}
					if (titre.length() > 1) {
						titre = titre.replace('_', ' ');
						titre = titre.replace('/', ' ');
						titre = titre.replace('\\', ' ');
						titre = titre.replace('.', ' ');
						titre = titre.substring(0, 1).toUpperCase() + titre.substring(1);
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
				} catch (Exception e) {
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
				File fileDest = new File(file.getParentFile() + "/" + newFileName.getText());
				if (fileDest.exists()) {
					MessageBox msg = new MessageBox(shell);
					msg.setMessage("Il existe déjà  un fichier avec ce nom dans le repertoire!!!");
					msg.open();
				} else {
					file.renameTo(fileDest);
					System.out.println(file.getParentFile() + "/" + newFileName.getText());
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
				if (list.getSelection() != null && list.getSelection().length > 0) {
					getTask(folderPath.getText() + "/" + list.getSelection()[0].toString()).start();
				} else if (listLeft.getSelection() != null && listLeft.getSelection().length > 0)
					getTask(folderPathLeft.getText() + "/" + listLeft.getSelection()[0].toString()).start();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		stopButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {

				stop();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

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
				if (child.isFile() && child.getName().toLowerCase().endsWith(".mp3")) {
					all.add(child.getName());
				}
				// addTree(child, all);
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

	static void scanLeft() {
		reset();
		ArrayList<String> aList = new ArrayList<String>();
		addTree(new File(folderPathLeft.getText()), aList);
		Collections.sort(aList);
		listLeft.setItems(aList.toArray(new String[aList.size()]));
		listLeft.redraw();
	}

	public static Thread getTask(String fileName) {
		final String fFileName = fileName;
		if (player != null) {
			player.close();
		}
		if (playerThread != null) {
			playerThread.interrupt();
		}

		playerThread = new Thread() {
			public void run() {
				try {

					System.out.println("playing " + fFileName + "...");
					InputStream in = new FileInputStream(new File(fFileName));

					AudioDevice dev = FactoryRegistry.systemRegistry().createAudioDevice();
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

	public static void stop() {
		if (player != null) {
			player.close();
		}
		if (playerThread != null) {
			playerThread.interrupt();
		}
	}
}
