## Support

#### **Installing GAMA extensions**

Use **Support > Install GAMA extensions...** in a packaged GAMA installation,
then choose the extension to install. The submenu discovers installable features
from <https://updates.gama-platform.org/plugin/2026-06/> and
<https://gama-platform.org/gama.copilot/> in the background when first opened.
It keeps the list in memory and refreshes it on opening after an hour; use
**Refresh extension list** to check immediately. The two sites load independently,
and entries appear as each site finishes. Refresh jobs are visible in Eclipse's
Progress view; **Cancel refresh** requests cancellation of outstanding reads.
Entries are grouped by their source repository, with its last word capitalized
(for example, `Inrae` or `Copilot`) shown as a disabled gray heading.
Groups and their entries are sorted alphabetically. Entries are temporarily named
**Install &lt;Name&gt; extension**, using the last one or two
words of the repository name after `plugin`, `extension` or `gama`. Repositories
publishing multiple features use feature identifiers to distinguish their entries.
Only features advertised in p2 metadata are listed: a directory on the web server
that is not included in the composite repository does not appear.
Unavailable sites are reported, and any previously discovered entries for those
sites remain available until a successful refresh.

GAMA selects the feature and update sites,
resolves dependencies in the background, and opens an installation review without
requiring you to browse repositories or select individual plug-ins. Review the
changes, accept the applicable licenses, confirm trust when prompted, and restart
GAMA when Eclipse requests it. License and signature checks are not bypassed.

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
Already-installed features are reported rather than reinstalled; use
**Support > Check for Updates** to update them.
For Copilot, the installed check uses the GAMA wrapper feature: having only the
upstream extension installed does not prevent adding the GAMA integration.

These entries cover installation only. GitHub sign-in, Copilot entitlement,
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
