package reivajh06.loaders;

import reivajh06.exceptions.LoaderException;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class URLMobLoader implements Loader {

	private final URL url;

	public URLMobLoader(String url) {
		try {
			this.url = URI.create(url).toURL();

		} catch (MalformedURLException e) {
			throw new LoaderException("Invalid url: %s".formatted(url));
		}
	}

	@Override
	public List<String> load() {
		try {
			List<String> load = load(url.openStream());
			load.removeFirst();
			return load;

		} catch (IOException e) {
			throw new LoaderException("Error while trying to load mob source via url: %s".formatted(url));
		}
	}
}
