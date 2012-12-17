package me.blablubbabc.paintball;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;

public class Musiker {
	private static Plugin plugin;
	private static File path;

	public Musiker() {
	}

	public static Melody loadMelody(Plugin plugin, String fileName, boolean nbs) {
		// init
		Musiker.plugin = plugin;
		path = new File(plugin.getDataFolder().toString());
		if (!path.exists())
			path.mkdirs();

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
		Melody melody = loadMelody(melodyFile, true);
		if (melody == null) {
			log("ERROR: Couldn't load the specified melody file!");
			log("Do you use a valid melody file?");
			return null;
		} else
			return melody;
	}

	@SuppressWarnings("unused")
	private static Melody loadMelody(File file, boolean nbs) {
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
							melody.addTon(new Ton(sound, id, delay));
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
			LEDataInputStream scanner = null;
			try {
				scanner = new LEDataInputStream(new FileInputStream(file));
				// header
				short length = scanner.readShort();
				short height = scanner.readShort();
				String name = scanner.readString();
				String author = scanner.readString();
				String origAuthor = scanner.readString();
				String description = scanner.readString();
				short tempo = scanner.readShort();
				//tempo
				if(tempo != 1000 && tempo != 500 && tempo != 250) {
					log("ERROR: Not supported tempo: "+tempo);
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
							log("ERROR: Couldn't get a instrument right: "+inst);
							return null;
						}
						byte key = scanner.readByte();
						key -= 33;
						if (key < 0 || key > 24) {
							log("ERROR: Couldn't get a note right: "+key);
							return null;
						}
						// System.out.print(sound.toString() + " / " + key +
						// " / " + tick);
						melodie.addTon(new Ton(sound, key, (tick * getDelay(tempo))));
					}
				}
				log("Scanned .nbt melody sucessfully.");
				return melodie;
			} catch (Exception e) {
				log("ERROR: Couldn't load the specified melody file.");
				e.printStackTrace();
				return null;
			} finally {
				if (scanner != null)
					try {
						scanner.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}
	
	private static int getDelay(int tempo) {
		switch(tempo) {
		case 1000: return 2;
		case 500: return 4;
		case 250: return 8;
		default: return 0;
		}
	}
	
	private static enum Instrus {
		PI, BG, BD, SD, ST, PL
	}

	private static Sound getSound(String s) {
		try {
			Instrus i = Instrus.valueOf(s.toUpperCase());
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

	private static Sound getSound(byte b) {
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

	private static Integer getNoteId(String s) {
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

	private static void log(String message) {
		System.out.println("[" + plugin.getName() + "] " + message);
	}
}