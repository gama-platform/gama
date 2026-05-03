cat << 'INNER_EOF' > /tmp/patch.diff
<<<<<<< SEARCH
				final IList<IShape> pts = GamaListFactory.create(Types.POINT);
				for (final IShape ent : segments) {
					for (final IPoint p : GeometryUtils.getPointsOf(ent)) { if (!pts.contains(p)) { pts.add(p); } }
				}
				if (pts.size() > 1) { shape = GamaShapeFactory.buildPolyline(pts); }
=======
				final java.util.Set<IPoint> ptsSet = new java.util.LinkedHashSet<>();
				for (final IShape ent : segments) {
					for (final IPoint p : GeometryUtils.getPointsOf(ent)) { ptsSet.add(p); }
				}
				if (ptsSet.size() > 1) { shape = GamaShapeFactory.buildPolyline(GamaListFactory.createWithoutCasting(Types.POINT, ptsSet)); }
>>>>>>> REPLACE
INNER_EOF
