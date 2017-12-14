import java.io.File;
import java.io.FilenameFilter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameTracker {

	private static List<Class<Servable>> gameList;
	private static Map<Class<Servable>, String> gameInfo;

	/**
	 * Looks inside the current working directory and collects all file names having
	 * the extension .class
	 * 
	 * @return an array of files
	 */
	private static File[] findClassFilesInWorkingDirectory() {
		File directory = new File(".");
		File[] files = directory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith("class");
			}
		});
		return files;
	}

	public static String buildGameListMenu() {
		String s = "*****\tGAME LIST\n";
		int i = 0;
		for (Class<Servable> c : gameList) {
			s += (i++) + "\t" + c.getName() + "\n";
		}
		s+="\nEnter the number of the game to play or 'q' to exit.\n";
		return s;
	}

	// verify that parameter refers to a game in the game bank
	public static boolean checkValidInteger(String s) {
		try {
			int i = Integer.parseInt(s);
			if (i < 0)
				return false;
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Examines user selection and returns either a String holding a menu, a new
	 * instance of the requested game, or null if neither of the above makes sense.
	 * 
	 * @param userSelection
	 * @return an object representing a menu string, a game instance or null
	 */
	public static Object handleUserSelection(String userSelection) {
		Object o = null;
		if (GameTracker.checkValidInteger(userSelection)) {
			try {
				o = gameList.get(Integer.parseInt(userSelection))
						.newInstance();
			} catch (NumberFormatException | InstantiationException
					| IllegalAccessException e) {
				// game not instantiated -- leave object null
				return e;
			}
		} else {
			o = buildGameListMenu();
		}
		return o;
	}

	/**
	 * Returns members of the authors array in a well formed String
	 * 
	 * @param authors
	 * @return comma separated list of authors
	 */
	private static String formatAuthorString(String[] authors) {
		String authorString = "";
		for (int i = 0; i < authors.length - 1; i++) {
			authorString += authors[i];
			if (i < authors.length - 2)
				authorString += ", ";
			else
				authorString += " and "; // no Oxford commas
		}
		authorString += authors[authors.length - 1];
		return authorString;
	}

	/**
	 * Use for verifying classpath is suitable for loading games
	 * 
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void main(String[] args)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		//List<Class<Servable>> classes = findServableClasses();
		initialize();
		for (Class<Servable> c : gameInfo.keySet()) {
			Annotation[] annotations = c.getAnnotations();
			for (Annotation a : annotations) {
				if (a instanceof GameInfo) {
					GameInfo info = (GameInfo) a;
					String authors = formatAuthorString(
							info.authors());
					System.out.println(info.gameTitle()
							+ ". Written by " + authors
							+ ".  Version: " + info.version());
				}
			}
			File f = new File(c.getName() + ".class");
			Date d = new Date(f.lastModified());
			System.out.println("Last modified :" + d);
		}
		System.out.println("Initialization complete.  Map is "+gameInfo);
	}

	/**
	 * Collects from the working directory all files that implement the Servable
	 * interface
	 * 
	 * @return a list holding all classes that implement Servable
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Class<Servable>> findServableClasses()
			throws ClassNotFoundException {
		List<Class<Servable>> servableClasses = new ArrayList<Class<Servable>>();
		for (File f : findClassFilesInWorkingDirectory()) {
			String nameWithExtension = f.getName();
			int idx = nameWithExtension.lastIndexOf(".class");
			Class classObj = Class
					.forName(nameWithExtension.substring(0, idx));
			Class[] interfaces = classObj.getInterfaces();
			if (interfaces.length > 0) {
				for (Class c : interfaces) {
					if (Servable.class.isAssignableFrom(c)) {
						servableClasses.add(classObj);
						break;
					}
				}
			}
		}
		return servableClasses;
	}

	/**
	 * Initializes the game database
	 * 
	 */
	public static void initialize() {
		try {
			gameInfo = new HashMap<Class<Servable>, String>();
			gameList = findServableClasses();
			for (Class<Servable> c : gameList) {
				Annotation[] annotations = c.getAnnotations();
				for (Annotation a : annotations) {
					if (a instanceof GameInfo) {
						GameInfo info = (GameInfo) a;
						gameInfo.put(c, formatGameInfoString(info));
					} else {
						gameInfo.put(c, null);
					}
				}
			}

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String formatGameInfoString(GameInfo g) {
		String s = "========== ";
		s+="\t"+g.gameTitle() +"\n";
		s+="\t"+g.description()+"\n";
		s+="\t"+formatAuthorString(g.authors());
		s+="\t"+g.version();
		return s;
	}
	
	public static String getGameInfo(Class<Servable> c) {
		if (gameInfo.containsKey(c)) return gameInfo.get(c);
		return c+" -- no game information available";
	}
}
