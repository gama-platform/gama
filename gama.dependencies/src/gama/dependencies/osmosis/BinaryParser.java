/**
 * Copyright (c) 2010 Scott A. Crosby. <scott@sacrosby.com>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */

package gama.dependencies.osmosis;

import java.util.Date;
import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;

public abstract class BinaryParser implements BlockReaderAdapter {
	protected int granularity;
	private long lat_offset;
	private long lon_offset;
	protected int date_granularity;
	private String strings[];

	/** Take a Info protocol buffer containing a date and convert it into a java Date object */
	protected Date getDate(final Osmformat.Info info) {
		if (info.hasTimestamp()) {
			return new Date(date_granularity * info.getTimestamp());
		} else {
			return NODATE;
		}
	}

	public static final Date NODATE = new Date(-1);

	/**
	 * Get a string based on the index used.
	 *
	 * Index 0 is reserved to use as a delimiter, therefore, index 1 corresponds to the first string in the table
	 *
	 * @param id
	 * @return
	 */
	protected String getStringById(final int id) {
		return strings[id];
	}

	@Override
	public void handleBlock(final FileBlock message) {

		try {
			if (message.getType().equals("OSMHeader")) {
				final Osmformat.HeaderBlock headerblock = Osmformat.HeaderBlock.parseFrom(message.getData());
				parse(headerblock);
			} else if (message.getType().equals("OSMData")) {
				final Osmformat.PrimitiveBlock primblock = Osmformat.PrimitiveBlock.parseFrom(message.getData());
				parse(primblock);
			}
		} catch (final InvalidProtocolBufferException e) {

			e.printStackTrace();
			throw new Error("ParseError"); // TODO
		}

	}

	@Override
	public boolean skipBlock(final FileBlockPosition block) {
		if (block.getType().equals("OSMData")) { return false; }
		if (block.getType().equals("OSMHeader")) { return false; }
		return true;
	}

	/** Convert a latitude value stored in a protobuf into a double, compensating for granularity and latitude offset */
	public double parseLat(final long degree) {
		// Support non-zero offsets. (We don't currently generate them)
		return (granularity * degree + lat_offset) * .000000001;
	}

	/**
	 * Convert a longitude value stored in a protobuf into a double, compensating for granularity and longitude offset
	 */
	public double parseLon(final long degree) {
		// Support non-zero offsets. (We don't currently generate them)
		return (granularity * degree + lon_offset) * .000000001;
	}

	/** Parse a Primitive block (containing a string table, other paramaters, and PrimitiveGroups */
	public void parse(final Osmformat.PrimitiveBlock block) {
		final Osmformat.StringTable stablemessage = block.getStringtable();
		strings = new String[stablemessage.getSCount()];

		for (int i = 0; i < strings.length; i++) { strings[i] = stablemessage.getS(i).toStringUtf8(); }

		granularity = block.getGranularity();
		lat_offset = block.getLatOffset();
		lon_offset = block.getLonOffset();
		date_granularity = block.getDateGranularity();

		for (final Osmformat.PrimitiveGroup groupmessage : block.getPrimitivegroupList()) {
			// Exactly one of these should trigger on each loop.
			parseNodes(groupmessage.getNodesList());
			parseWays(groupmessage.getWaysList());
			parseRelations(groupmessage.getRelationsList());
			if (groupmessage.hasDense()) { parseDense(groupmessage.getDense()); }
		}
	}

	/** Parse a list of Relation protocol buffers and send the resulting relations to a sink. */
	protected abstract void parseRelations(List<Osmformat.Relation> rels);

	/** Parse a DenseNode protocol buffer and send the resulting nodes to a sink. */
	protected abstract void parseDense(Osmformat.DenseNodes nodes);

	/** Parse a list of Node protocol buffers and send the resulting nodes to a sink. */
	protected abstract void parseNodes(List<Osmformat.Node> nodes);

	/** Parse a list of Way protocol buffers and send the resulting ways to a sink. */
	protected abstract void parseWays(List<Osmformat.Way> ways);

	/** Parse a header message. */
	protected abstract void parse(Osmformat.HeaderBlock header);

}