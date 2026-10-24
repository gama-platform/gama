package gama.core.util.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.ContributorFactorySimple;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.spi.RegistryStrategy;

/** Standalone regression checks for OBJ rendering cache keys and independent material loading (issue #644). */
public final class GeometryFileCacheKeyTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	public static void main(final String[] args) throws Exception {
		if (RegistryFactory.getRegistry() == null) {
			final Object token = new Object();
			final var registry = RegistryFactory.createRegistry(new RegistryStrategy(null, null), token, token);
			try (var jar = new JarFile(Path.of(EFS.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile());
					var plugin = jar.getInputStream(jar.getJarEntry("plugin.xml"))) {
				check(registry.addContribution(plugin,
						ContributorFactorySimple.createContributor("org.eclipse.core.filesystem"), false, null, null, token),
						"Standalone filesystem registry must initialize");
			}
			RegistryFactory.setDefaultRegistryProvider(() -> registry);
		}
		final Path directory = Files.createTempDirectory("gama-obj-materials-");
		final Path obj = directory.resolve("car.obj");
		final Path redMtl = directory.resolve("car.mtl");
		final Path greenMtl = directory.resolve("braking.mtl");
		try {
			Files.writeString(obj, """
					mtllib car.mtl
					v 0 0 0
					v 1 0 0
					v 0 1 0
					usemtl lights
					f 1 2 3
					""");
			Files.writeString(redMtl, "newmtl lights\nKd 0.2 0.0 0.0\n");
			Files.writeString(greenMtl, "newmtl lights\nKd 0.0 1.0 0.0\n");

			final GamaObjFile red = new GamaObjFile(null, obj.toString(), redMtl.toString());
			final GamaObjFile green = new GamaObjFile(null, obj.toString(), greenMtl.toString());
			final GamaObjFile duplicate = new GamaObjFile(null, obj.toString(), redMtl.toString());
			final GamaObjFile implicit = new GamaObjFile(null, obj.toString());
			final GamaObjFile noMaterial = new GamaObjFile(null, obj.toString(), (String) null);

			check(red.getPath(null).equals(green.getPath(null)), "Material variants keep the same physical OBJ path");
			check(!red.getDrawingCacheKey(null).equals(green.getDrawingCacheKey(null)),
					"Different materials must produce distinct rendering cache entries");
			check(red.getDrawingCacheKey(null).equals(duplicate.getDrawingCacheKey(null)),
					"Repeated OBJ/MTL pairs must reuse the same rendering cache entry");
			check(red.getDrawingCacheKey(null).equals(implicit.getDrawingCacheKey(null)),
					"Implicit and explicit default materials must share a rendering cache entry");
			check(!red.getDrawingCacheKey(null).equals(noMaterial.getDrawingCacheKey(null)),
					"An OBJ without materials must not share a materialized rendering entry");
			check(!noMaterial.getDrawingCacheKey(null).equals(green.getDrawingCacheKey(null)),
					"An OBJ without materials must remain distinct from all material variants");

			final Map<String, GamaGeometryFile> entries = new HashMap<>();
			entries.putIfAbsent(red.getDrawingCacheKey(null), red);
			entries.putIfAbsent(green.getDrawingCacheKey(null), green);
			entries.putIfAbsent(duplicate.getDrawingCacheKey(null), duplicate);
			check(entries.size() == 2, "Registration must retain both variants while deduplicating identical pairs");
			for (final GamaGeometryFile file : entries.values()) {
				((GamaObjFile) file).loadObject(null, true);
			}
			check(red.materials != null && green.materials != null, "Both material libraries must load");
			check(red.materials.getKd("lights")[0] > 0 && red.materials.getKd("lights")[1] == 0,
					"The first OBJ variant must retain its red material");
			check(green.materials.getKd("lights")[0] == 0 && green.materials.getKd("lights")[1] == 1,
					"The second OBJ variant must retain its green material");

			final GamaObjFile first = new GamaObjFile(null, "a.obj", "b.objc.mtl");
			final GamaObjFile second = new GamaObjFile(null, "a.objb.obj", "c.mtl");
			check(!first.getDrawingCacheKey(null).equals(second.getDrawingCacheKey(null)),
					"Path pairs with identical plain concatenations must not collide");
		} finally {
			Files.deleteIfExists(obj);
			Files.deleteIfExists(redMtl);
			Files.deleteIfExists(greenMtl);
			Files.delete(directory);
		}
	}
}
