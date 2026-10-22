package reivajh06.loaders;

import reivajh06.exceptions.LoaderException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FileMobLoader implements Loader {

	private final File file;

	public FileMobLoader(File file) {
		this.file = file;
	}

	@Override
	public List<String> load() {
		try {
			List<String> load = load(new FileInputStream(file));
			load.removeFirst();
			return load;

		} catch (IOException e) {
			throw new LoaderException("Error while trying to load mob source file");
		}
	}
}
