package de.blablubbabc.paintball.addons.melodies;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Musician {
	private Plugin plugin;
	private File path;
	
	public boolean success;

	public Melody winDef;
	public Melody defeatDef;
	public Melody drawDef;
	
	public Melody win;
	public Melody defeat;
	public Melody draw;

	public Musician(Plugin plugin, String winFile, boolean winNbs, String defeatFile, boolean defeatNbs,
			String drawFile, boolean drawNbs) {
		// init
		this.plugin = plugin;
		success = true;
		path = new File(plugin.getDataFolder().toString() + "/melodies/");
		if (!path.exists())
			path.mkdirs();
		
		// defaults
		if(!writeDefaultMelodyFile("win")) success = false;
		if(!writeDefaultMelodyFile("defeat")) success = false;
		if(!writeDefaultMelodyFile("draw")) success = false;
		if(!success) return;
		winDef = loadMelody("win", false);
		defeatDef = loadMelody("defeat", false);
		drawDef = loadMelody("draw", false);
		if(winDef == null || defeatDef == null || drawDef == null) success = false;
		if(!success) return;
		
		// speziell
		if (!winFile.equals("win") || winNbs) {
			win = loadMelody(winFile, winNbs);
			if (win == null) {
				log("ERROR: Something went wrong with the win melody scanning. Using the default melody now.");
			}
		}
		if (!defeatFile.equals("defeat") || defeatNbs) {
			defeat = loadMelody(defeatFile, defeatNbs);
			if (defeat == null) {
				log("ERROR: Something went wrong with the defeat melody scanning. Using the default melody now.");
			}
		}
		if (!drawFile.equals("draw") || drawNbs) {
			draw = loadMelody(drawFile, drawNbs);
			if (draw == null) {
				log("ERROR: Something went wrong with the draw melody scanning. Using the default melody now.");
			}
		}
	}
	
	public void playWin(Player p) {
		if (win == null) winDef.play(plugin, p); 
		else win.play(plugin, p);
	}
	public void playDefeat(Player p) {
		if (defeat == null) defeatDef.play(plugin, p); 
		else defeat.play(plugin, p);
	}
	public void playDraw(Player p) {
		if (draw == null) drawDef.play(plugin, p); 
		else draw.play(plugin, p);
	}

	public boolean writeDefaultMelodyFile(String fileName) {
		// defaults
		File def_file = new File(path + "/" + fileName + ".txt");
		InputStream in = null;
		OutputStream out = null;
		try {
			in = plugin.getResource(fileName + ".txt");
			if (in != null) {
				out = new FileOutputStream(def_file);
				byte[] buffer = new byte[10240];
				int len = in.read(buffer);
				while (len != -1) {
					out.write(buffer, 0, len);
					len = in.read(buffer);
				}
			} else {
				log("ERROR: Couldn't load the default " + fileName
						+ " melody file from jar!");
				return false;
			}
		} catch (Exception e) {
			log("ERROR: Couldn't write the default " + fileName
					+ " melody file!");
			e.printStackTrace();
			return false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	private Melody loadMelody(String fileName, boolean nbs) {
		File melodyFile;
		// get melody:
		if (!nbs) {
			// .txt
			melodyFile = new File(path + "/" + fileName + ".txt");
			if (!melodyFile.exists()) {
				log("ERROR: Couldn't find the specified melody file.");
				return null;
			}
		} else {
			// .nbs
			melodyFile = new File(path + "/" + fileName + ".nbs");
			if (!melodyFile.exists()) {
				log("ERROR: Couldn't find the specified melody file.");
				return null;
			}
		}
		log("Loading the specified melody now: " + melodyFile.getName());
		Melody melody = loadMelody(melodyFile, nbs);
		if (melody == null) {
			log("ERROR: Couldn't load the specified melody file!");
			log("Do you use a valid melody file?");
			return null;
		} else
			return melody;
	}

	@SuppressWarnings("unused")
	private Melody loadMelody(File file, boolean nbs) {
		if (!nbs) {
			// .txt
			Melody melody = new Melody();
			Scanner scanner = null;
			try {
				scanner = new Scanner(file);
				int line = 0;
				while (scanner.hasNextLine()) {
					line++;
					String text = scanner.nextLine();
					// Spaces entfernen
					text.replaceAll(" ", "");
					// get tones:
					String[] tones = text.split("-");
					for (String ts : tones) {
						if (!ts.isEmpty()) {
							String[] ton = ts.split(":");
							if (ton.length != 2) {
								log("ERROR: Couldn't get the note in line: "
										+ line);
								return null;
							}
							// sound
							Sound sound = getSound(ton[0]);
							if (sound == null) {
								log("ERROR: Couldn't get the instrument in line: "
										+ line);
								return null;
							}
							// note (id between 0-24)
							Integer id = getNoteId(ton[1]);
							if (id == null || id < 0 || id > 24) {
								log("ERROR: Couldn't get a valid note id in line: "
										+ line);
								return null;
							}
							// delay in ticks (line * 2 ticks):
							long delay = (line - 1) * 2;
							// add note to melodie:
							melody.addTon(new Note(sound, id, delay));
						}
					}
				}
				log("Scanned .txt melody sucessfully. Lines: " + line);
				return melody;
			} catch (Exception e) {
				log("ERROR: Couldn't load the specified melody file.");
				e.printStackTrace();
				return null;
			} finally {
				if (scanner != null)
					scanner.close();
			}
		} else {
			// ./nbs
			Melody melodie = new Melody();
			LittleEndianStream scanner = null;
			try {
				scanner = new LittleEndianStream(new FileInputStream(file));
				// header
				short length = scanner.readShort();
				short height = scanner.readShort();
				String name = scanner.readString();
				String author = scanner.readString();
				String origAuthor = scanner.readString();
				String description = scanner.readString();
				short tempo = scanner.readShort();
				// tempo
				if (tempo != 1000 && tempo != 500 && tempo != 250) {
					log("ERROR: Not supported tempo: " + tempo);
					return null;
				}

				byte autoSaving = scanner.readByte();
				byte autoSaveDuration = scanner.readByte();
				byte timeSignature = scanner.readByte();
				int minutesSpent = scanner.readInt();
				int leftClicks = scanner.readInt();
				int rightClicks = scanner.readInt();
				int blocksAdded = scanner.readInt();
				int blocksRemoved = scanner.readInt();
				String midiName = scanner.readString();

				// log them:
				/*
				 * log("length: " + length); log("height: " + height);
				 * log("name: " + name); log("author: " + author);
				 * log("origAuthor: " + origAuthor); log("description: " +
				 * description); log("tempo: " + tempo); log("autoSaving: " +
				 * autoSaving); log("autoSaveDuration: " + autoSaving);
				 * log("timeSignature: " + timeSignature); log("minutesSpent: "
				 * + minutesSpent); log("leftClicks: " + leftClicks);
				 * log("rightClicks: " + rightClicks); log("blocksAdded: " +
				 * blocksAdded); log("blocksRemoved: " + blocksRemoved);
				 * log("midiName: " + midiName);
				 */

				// notes
				short tick = -1;
				short jumps = 0;
				while (true) {
					jumps = scanner.readShort();
					if (jumps == 0) {
						break;
					}
					tick += jumps;
					while (true) {
						jumps = scanner.readShort();
						if (jumps == 0) {
							break;
						}
						byte inst = scanner.readByte();
						Sound sound = getSound(inst);
						if (sound == null) {
							log("ERROR: Couldn't get a instrument right: " + inst);
							return null;
						}
						byte key = scanner.readByte();
						key -= 33;
						if (key < 0 || key > 24) {
							log("ERROR: Couldn't get a note right: " + key);
							return null;
						}
						// System.out.print(sound.toString() + " / " + key +
						// " / " + tick);
						melodie.addTon(new Note(sound, key,
								(tick * getDelay(tempo))));
					}
				}
				log("Scanned .nbt melody sucessfully.");
				return melodie;
			} catch (Exception e) {
				log("ERROR: Couldn't load the specified melody file.");
				e.printStackTrace();
				return null;
			} finally {
				if (scanner != null) {
					try {
						scanner.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private int getDelay(int tempo) {
		switch (tempo) {
		case 1000:
			return 2;
		case 500:
			return 4;
		case 250:
			return 8;
		default:
			return 0;
		}
	}

	private Sound getSound(String s) {
		try {
			Instrument i = Instrument.valueOf(s.toUpperCase());
			switch (i) {
			case PI:
				return Sound.NOTE_PIANO;
			case BG:
				return Sound.NOTE_BASS_GUITAR;
			case BD:
				return Sound.NOTE_BASS_DRUM;
			case SD:
				return Sound.NOTE_SNARE_DRUM;
			case ST:
				return Sound.NOTE_STICKS;
			case PL:
				return Sound.NOTE_PLING;
			default:
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	private Sound getSound(byte b) {
		try {
			switch (b) {
			case 0:
				return Sound.NOTE_PIANO;
			case 1:
				return Sound.NOTE_BASS_GUITAR;
			case 2:
				return Sound.NOTE_BASS_DRUM;
			case 3:
				return Sound.NOTE_SNARE_DRUM;
			case 4:
				return Sound.NOTE_STICKS;
			default:
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	private Integer getNoteId(String s) {
		try {
			Integer id = Integer.parseInt(s);
			if (id < 0 || id > 24)
				return null;
			else
				return id;
		} catch (Exception e) {
			return null;
		}
	}

	private void log(String message) {
		System.out.println("[" + plugin.getName() + "] " + message);
	}
}