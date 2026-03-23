/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * The message package provides support for agent communication through message passing in GAMA.
 * 
 * <p>
 * This package contains the interfaces and factories for creating and managing messages exchanged between agents in
 * multi-agent simulations. Messages are fundamental to implementing communication protocols and information exchange in
 * agent-based models.
 * </p>
 * 
 * <h2>Main Components</h2>
 * <ul>
 * <li><strong>{@link gama.api.types.message.IMessage}</strong> - The core interface representing a message with
 * sender, receivers, contents, and timestamp information. Messages track whether they have been read and when they were
 * sent and received.</li>
 * 
 * <li><strong>{@link gama.api.types.message.GamaMessageFactory}</strong> - The static factory for creating message
 * instances. Provides convenient methods to construct messages with various combinations of sender, receivers, and
 * contents.</li>
 * 
 * <li><strong>{@link gama.api.types.message.IMessageFactory}</strong> - The internal factory interface that
 * implementations must fulfill. This allows for pluggable message implementations.</li>
 * </ul>
 * 
 * <h2>Message Attributes</h2>
 * <p>
 * Messages in GAMA contain the following information:
 * </p>
 * <ul>
 * <li><strong>sender</strong> - The agent or entity that sent the message</li>
 * <li><strong>receivers</strong> - The intended recipient(s) of the message</li>
 * <li><strong>contents</strong> - The payload of the message (can be any GAMA value or object)</li>
 * <li><strong>emission_timestamp</strong> - The simulation cycle when the message was sent</li>
 * <li><strong>reception_timestamp</strong> - The simulation cycle when the message was received</li>
 * <li><strong>unread</strong> - Boolean flag indicating whether the message has been read</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <pre>
 * // Create a simple message
 * IMessage msg = GamaMessageFactory.create(scope, sender, receivers, "Hello!");
 * 
 * // Cast an object to a message
 * IMessage msg2 = GamaMessageFactory.castToMessage(scope, agent, someContent);
 * 
 * // Check message status
 * if (msg.isUnread()) {
 *     Object content = msg.getContents();
 *     msg.setUnread(false);
 * }
 * 
 * // Get timestamp information
 * int sentAt = msg.getEmissionTimestamp();
 * int receivedAt = msg.getReceptionTimestamp();
 * </pre>
 * 
 * <h2>Communication Protocols</h2>
 * <p>
 * Messages are typically used in conjunction with FIPA (Foundation for Intelligent Physical Agents) protocols or custom
 * communication protocols. The message package provides the basic infrastructure, while higher-level protocols are
 * implemented in extension packages.
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
package gama.api.types.message;
