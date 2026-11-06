## Support

#### **Installing GAMA extensions**

Use **Support > Install GAMA extensions...** in a packaged GAMA installation,
then **Choose plugins to install...** to open the simplified installer.
Check individual plugins or entire categories, or use **Select all** and
**Deselect all**. Installed plugins remain visible but cannot be selected again;
older installed versions are offered as updates. A plugin listed in several
categories shares the same selection and is installed only once.
Click **Next** to resolve all selected plugins together, then review the selected
plugins and the complete dependency additions, replacements and removals.
**Finish** schedules a single background installation and Eclipse prompts for a
restart when it completes. **Back** lets you change the selection; canceling
dependency resolution does not install anything.
Individual extension menu entries open the same wizard with that feature selected.

The submenu and wizard share a catalogue of installable features discovered
from the main GAMA repository, <https://updates.gama-platform.org/2026-06/>,
the optional plugin repository, <https://updates.gama-platform.org/plugin/2026-06/>, and
<https://gama-platform.org/gama.copilot/> in background jobs started during
workbench startup, without waiting for the menu to be opened or blocking startup.
The startup discovery and menu share the same in-memory list and running jobs.
Discovery is disabled in exported-player mode.
It keeps the list in memory and refreshes it on opening after an hour; use
**Refresh extension list** to check immediately. The three sites load independently,
and entries appear as each site finishes. Refresh jobs are visible in Eclipse's
Progress view; the submenu's **Cancel refresh** requests cancellation of outstanding reads.
The wizard fills as sites finish loading and has its own **Refresh list** button.
Entries are grouped by the category labels published in the update sites' p2
metadata, shown as submenus in the menu and checkable groups in the wizard.
Categories with the same label across sites share a group; features in multiple
categories appear in each group.
Features without a category appear under **Uncategorized**.
Groups and their entries are sorted alphabetically. Entries are named
**Install &lt;Name&gt; extension...**, using the published feature name (or its
identifier when no name is published). All installable features from the main
GAMA repository are included, not just optional plugins. When a feature is
published more than once, the newest version is offered.
Only features advertised in p2 metadata are listed: a directory on the web server
that is not included in the composite repository does not appear.
Unavailable sites are reported, and any previously discovered entries for those
sites remain available until a successful refresh.

GAMA selects the update sites and resolves dependencies without requiring you to
browse repositories or select individual dependency plug-ins.
Resolution first uses the selected plugins' sites and the installed profile.
The Eclipse 2026-03 repository is consulted only if that cannot resolve the
selection, avoiding unrelated optional Eclipse components when the installed
platform already satisfies the plugins. Synthetic Java execution-environment
units from update sites are excluded: installing plugins must not replace the
Java environment recorded for GAMA.
Finishing the simplified installer automatically accepts the licenses of all
added plugins and dependencies, including installer components.
**Trust all plugin publishers, including unsigned plugins** is enabled by default
on the review page. It skips p2's signing-certificate and unsigned-code trust
prompts for this installation only, including its dependencies. Uncheck it to
retain Eclipse's normal trust verification and prompts. This does not change
global Eclipse trust settings or the general **Install other extensions...**
installer, and download integrity checks remain active.
**Plugins execute code with your permissions; automatic trust does not establish
that a plugin is safe. Only install software from sources you trust.**

Diagramming installs the GAMA graphical modeling feature, including Graphiti,
GEF and its EMF dependencies, from the 2026-06 graphical modeling update site.
Copilot installs the optional `gama.feature.copilot` feature from its independent
thin update site, <https://gama-platform.org/gama.copilot/>. That site supplies only
the GAMA wrapper and integration plug-in; p2 obtains GitHub Copilot and its
platform-specific agent from Microsoft's update site. Both installers use the
named Eclipse 2026-03 repository for required platform dependencies, not a moving
latest-Eclipse channel. Copilot is resolved to the newest version satisfying the
wrapper's compatibility range and the installed platform's dependencies; a newer
upstream release requiring newer Eclipse does not authorize upgrading that baseline.
On each menu opening, feature versions in the installation profile are compared
with the discovered repository versions. Features already installed at the same
or a newer version are disabled and labeled **(Installed) &lt;Name&gt; extension...**.
An older installed version is labeled **Update &lt;Name&gt; extension...** and opens
the simplified wizard with that feature selected, using the same dependency repositories.
Installation and update preparation recheck the profile and repository before
opening their review wizard. **Support > Check for updates...** remains available
to check all installed software.
For Copilot, the installed check uses the GAMA wrapper feature: having only the
upstream extension installed does not prevent adding the GAMA integration.

**Support > Install other extensions...**, directly below the GAMA extensions
submenu, opens the general p2 installer for other update sites. Below the separator,
**Check for updates...** checks installed software and **Manage extensions...**
opens installation details.

These entries cover installation and updates only. GitHub sign-in, Copilot entitlement,
and graphical/Copilot integration with GAML are separate from installation.
Before opening the workbench, the optional integration seeds Copilot's configuration preferences to
skip Quick Start and disable automatic What's New pages. Existing settings are
preserved; users can re-enable What's New in Copilot's General preferences.
The Quick Start marker is based on Copilot 0.21 and should be reviewed on upgrades.
Network, unavailable-feature and dependency-resolution failures are reported;
they do not trigger installation from another update channel.

Copilot packaging, dependency baselines, publishing and integration-specific issues
are maintained in [gama.copilot](https://github.com/gama-platform/gama.copilot).
The feature is no longer built or published by GAMA's Maven reactor.
Use that repository's documented build to publish Copilot updates independently.
The wrapper requires the upstream feature rather than duplicating its
platform-specific plug-in list. Third-party binaries are not mirrored on the
GAMA Copilot site; installation needs access to Microsoft and Eclipse as well.

For distribution maintainers: the Copilot update site must be published before
distributing the installer command. Its integration uses the
`gama.ui.application.startup` extension point for pre-workbench preference setup;
generic GAML editor extension support remains in GAMA.
Rebuild and publish the graphical modeling repository as well to apply its
transitive-dependency packaging setting. These baselines must be reviewed when
GAMA moves to a different Eclipse release or graphical modeling update channel.

#### **Do you think you found a bug?**

* **Ensure the bug was not already reported** by searching on GitHub under [Issues](https://github.com/gama-platform/gama/issues).

* If you're unable to find an open issue addressing the problem, [open a new one](https://github.com/gama-platform/gama/issues/new). Be sure to include a **title and clear description**, as much relevant information as possible, and a **code sample** or a **test case** demonstrating the expected behavior that is not occurring.

#### **Do you have questions about your models?**

* Ask any question about how to use GAMA in the [gama-platform mailing list](https://groups.google.com/forum/#!forum/gama-platform).

* Consult the documentation on the [website](http://gama-platform.org) or, alternatively, on the [wiki](https://github.com/gama-platform/gama/wiki).
</br>


Thanks!

GAMA Team
