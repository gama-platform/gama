sed -i '586,590c\
				final java.util.Set<IPoint> ptsSet = new java.util.LinkedHashSet<>();\
				for (final IShape ent : segments) {\
					for (final IPoint p : GeometryUtils.getPointsOf(ent)) { ptsSet.add(p); }\
				}\
				if (ptsSet.size() > 1) { \
					final IList<IShape> pts = GamaListFactory.create(Types.POINT, ptsSet.size());\
					pts.addAll(ptsSet);\
					shape = GamaShapeFactory.buildPolyline(pts); \
				}\
' ./gama.core/src/gama/core/util/path/GamaSpatialPath.java
