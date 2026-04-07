window.DOCS_DATA = {
  groups: [
    {
      title: 'Overview',
      pages: [
        { id: 'index', label: 'EzDonutStore' }
      ]
    },
    {
      title: 'Getting Started',
      pages: [
        { id: 'installation', label: 'Installation' },
        { id: 'configuration', label: 'Configuration' }
      ]
    },
    {
      title: 'Usage',
      pages: [
        { id: 'commands', label: 'Commands' },
        { id: 'permissions', label: 'Permissions' },
        { id: 'troubleshooting', label: 'Troubleshooting' }
      ]
    },
    {
      title: 'Features',
      pages: [
        { id: 'clickable-store-link', label: 'Store Message Flow' },
        { id: 'placeholderapi', label: 'PlaceholderAPI' }
      ]
    },
    {
      title: 'More',
      pages: [
        { id: 'faq', label: 'FAQ' },
        { id: 'changelog', label: 'Changelog' }
      ]
    }
  ],
  pages: {
    index: {
      title: 'EzDonutStore Docs',
      topbarTitle: 'EzDonutStore',
      meta: ['Overview', 'Home'],
      content: `
        <h1>EzDonutStore</h1>
        <p class="lead">EzDonutStore is a lightweight Paper and Folia plugin that gives players a polished <span class="inline-code">/store</span> command with a configurable clickable store link.</p>

        <section id="features"><h2>Features</h2>
          <ul>
            <li>Clickable store link with hover text</li>
            <li>Configurable chat, action bar, and reload messages</li>
            <li>Optional pling sound feedback</li>
            <li><span class="inline-code">/buy</span> alias for <span class="inline-code">/store</span></li>
            <li>Folia support</li>
            <li>PlaceholderAPI support</li>
            <li>Admin reload command</li>
          </ul>
        </section>

        <section id="downloads"><h2>Downloads</h2>
          <div class="table-wrap"><table>
            <thead><tr><th>Platform</th><th>Link</th></tr></thead>
            <tbody>
              <tr><td>GitHub Releases</td><td>Not configured in this repository</td></tr>
              <tr><td>SpigotMC</td><td>Not published</td></tr>
              <tr><td>BuiltByBit</td><td>Not published</td></tr>
            </tbody>
          </table></div>
        </section>

        <section id="bstats"><h2>bStats Total Servers</h2>
          <div class="stats-embed">
            <a href="https://bstats.org/plugin/bukkit/EzDonutStore" target="_blank" rel="noreferrer">
              <img src="https://bstats.org/signatures/bukkit/EzDonutStore.svg" alt="EzDonutStore bStats signature graph showing total servers" loading="lazy" />
            </a>
          </div>
        </section>

        <section id="requirements"><h2>Requirements</h2>
          <div class="table-wrap"><table>
            <thead><tr><th>Requirement</th><th>Version</th></tr></thead>
            <tbody>
              <tr><td>Java</td><td>21+</td></tr>
              <tr><td>Paper / Folia</td><td>1.21+</td></tr>
              <tr><td>Required Plugins</td><td>None</td></tr>
              <tr><td>Optional Plugins</td><td>PlaceholderAPI 2.11.6+</td></tr>
            </tbody>
          </table></div>
        </section>
      `
    },

    installation: {
      title: 'Installation · EzDonutStore Docs',
      topbarTitle: 'Installation',
      meta: ['Getting Started', 'Setup'],
      content: `
        <h1>Installation</h1>
        <section id="requirements"><h2>Requirements</h2>
          <ul>
            <li><strong>Java:</strong> 21+</li>
            <li><strong>Server Software:</strong> Paper or Folia 1.21+</li>
            <li><strong>Required Plugins:</strong> None</li>
            <li><strong>Optional Plugins:</strong> PlaceholderAPI 2.11.6+</li>
          </ul>
        </section>

        <section id="steps"><h2>Steps</h2>
          <ol>
            <li>Build with <span class="inline-code">mvn package</span> or use a release jar.</li>
            <li>Place <span class="inline-code">EzDonutStore-1.0.0.jar</span> into <span class="inline-code">/plugins/</span>.</li>
            <li>Restart to generate <span class="inline-code">/plugins/EzDonutStore/config.yml</span>.</li>
            <li>Update config with your store URL and messages.</li>
            <li>Run <span class="inline-code">/store</span> to verify output.</li>
          </ol>
          <p><strong>Warning:</strong> Do not use Bukkit <span class="inline-code">/reload</span>.</p>
        </section>

        <section id="updating"><h2>Updating</h2>
          <ol>
            <li>Stop server.</li>
            <li>Replace old jar.</li>
            <li>Start server.</li>
            <li>Check config keys before live reload.</li>
          </ol>
        </section>
      `
    },

    configuration: {
      title: 'Configuration · EzDonutStore Docs',
      topbarTitle: 'Configuration',
      meta: ['Getting Started', 'Configuration'],
      content: `
        <h1>Configuration</h1>
        <p class="lead">Config file path: <span class="inline-code">/plugins/EzDonutStore/config.yml</span>.</p>

        <section id="config-yml"><h2>config.yml</h2>
          <div class="code-block"><pre># EzDonutStore v1.0.0
link:
  raw: "https://store.donutsmp.net"
  display: "store.donutsmp.net"

message:
  store:
    enabled: true
    lines:
      - "&#00A4FCVisit the DonutSMP store"
      - "&#00A4FC&l*&r &f&n%link_display%"
    hover:
      - "&#00A4FCOpen the store link"
  actionbar:
    enabled: true
    text: "&7The link has been sent in chat!"
  reload:
    enabled: true
    lines:
      - "&#00A4FC&l*&r &f&nReloaded the config!"

sounds:
  pling:
    enabled: true
    sound: "BLOCK_NOTE_BLOCK_PLING"
    volume: 0.5
    pitch: 1.0

placeholderapi:
  enabled: true</pre></div>
        </section>

        <section id="placeholders"><h2>Message Placeholders</h2>
          <ul>
            <li><span class="inline-code">%link_raw%</span> from <span class="inline-code">link.raw</span></li>
            <li><span class="inline-code">%link_display%</span> from <span class="inline-code">link.display</span></li>
          </ul>
        </section>
      `
    },

    commands: {
      title: 'Commands · EzDonutStore Docs',
      topbarTitle: 'Commands',
      meta: ['Usage', 'Commands'],
      content: `
        <h1>Commands</h1>
        <section id="command-table"><h2>Command Reference</h2>
          <div class="table-wrap"><table>
            <thead><tr><th>Command</th><th>Description</th><th>Permission</th></tr></thead>
            <tbody>
              <tr><td><strong>/store</strong></td><td>Sends the configured store link.</td><td><span class="inline-code">ezdonutstore.store</span></td></tr>
              <tr><td><strong>/buy</strong></td><td>Alias of <span class="inline-code">/store</span>.</td><td><span class="inline-code">ezdonutstore.store</span></td></tr>
              <tr><td><strong>/store reload</strong></td><td>Reloads config and PlaceholderAPI integration.</td><td><span class="inline-code">ezdonutstore.reload</span></td></tr>
              <tr><td><strong>/buy reload</strong></td><td>Alias of <span class="inline-code">/store reload</span>.</td><td><span class="inline-code">ezdonutstore.reload</span></td></tr>
            </tbody>
          </table></div>
        </section>
      `
    },

    permissions: {
      title: 'Permissions · EzDonutStore Docs',
      topbarTitle: 'Permissions',
      meta: ['Usage', 'Permissions'],
      content: `
        <h1>Permissions</h1>
        <section id="permission-table"><h2>Permission Nodes</h2>
          <div class="table-wrap"><table>
            <thead><tr><th>Permission</th><th>Description</th><th>Default</th></tr></thead>
            <tbody>
              <tr><td><span class="inline-code">ezdonutstore.store</span></td><td>Allows <span class="inline-code">/store</span> and <span class="inline-code">/buy</span>.</td><td><span class="inline-code">true</span></td></tr>
              <tr><td><span class="inline-code">ezdonutstore.reload</span></td><td>Allows reload subcommands and tab completion.</td><td><span class="inline-code">op</span></td></tr>
              <tr><td><span class="inline-code">ezdonutstore.admin</span></td><td>Grants all permissions.</td><td><span class="inline-code">op</span></td></tr>
            </tbody>
          </table></div>
        </section>
      `
    },

    troubleshooting: {
      title: 'Troubleshooting · EzDonutStore Docs',
      topbarTitle: 'Troubleshooting',
      meta: ['Usage', 'Support'],
      content: `
        <h1>Troubleshooting</h1>

        <section id="quick-checks"><h2>Quick Checks</h2>
          <ol>
            <li>Java 21+ and Paper/Folia 1.21+</li>
            <li>Jar is in <span class="inline-code">/plugins/</span> and server restarted</li>
            <li>Config exists and YAML is valid</li>
            <li>Permissions are granted</li>
            <li>Run <span class="inline-code">/store reload</span> after edits</li>
          </ol>
        </section>

        <section id="symptom-guide"><h2>Symptom Guide</h2>
          <p>Most common causes: missing <span class="inline-code">ezdonutstore.store</span>/<span class="inline-code">ezdonutstore.reload</span>, bad <span class="inline-code">link.raw</span>, or missing <span class="inline-code">%link_display%</span>.</p>
        </section>
      `
    },

    'clickable-store-link': {
      title: 'Store Message Flow · EzDonutStore Docs',
      topbarTitle: 'Store Message Flow',
      meta: ['Features', 'Flow'],
      content: `
        <h1>Store Message Flow</h1>

        <section id="what-happens"><h2>What Happens On <span class="inline-code">/store</span> or <span class="inline-code">/buy</span></h2>
          <ol>
            <li>Permission check for <span class="inline-code">ezdonutstore.store</span> or <span class="inline-code">ezdonutstore.admin</span>.</li>
            <li>Reads <span class="inline-code">message.store.lines</span>.</li>
            <li>Clickable segment is created from <span class="inline-code">%link_display%</span> that opens <span class="inline-code">link.raw</span>.</li>
            <li>Optional hover text from <span class="inline-code">message.store.hover</span>.</li>
            <li>Optional action bar and pling sound for player senders.</li>
          </ol>
        </section>
      `
    },

    placeholderapi: {
      title: 'PlaceholderAPI · EzDonutStore Docs',
      topbarTitle: 'PlaceholderAPI',
      meta: ['Features', 'Integrations'],
      content: `
        <h1>PlaceholderAPI</h1>

        <section id="built-in"><h2>Built-In Placeholders</h2>
          <ul>
            <li><span class="inline-code">%ezdonutstore_link_raw%</span></li>
            <li><span class="inline-code">%ezdonutstore_link_display%</span></li>
          </ul>
        </section>

        <section id="parsing"><h2>Parsing Conditions</h2>
          <p>Parsing works only when PlaceholderAPI is installed, <span class="inline-code">placeholderapi.enabled</span> is true, and sender is a player.</p>
        </section>
      `
    },

    faq: {
      title: 'FAQ · EzDonutStore Docs',
      topbarTitle: 'FAQ',
      meta: ['More', 'Support'],
      content: `
        <h1>FAQ</h1>

        <section id="q1"><h2>Plugin is not loading</h2><p>Check Java 21+, Paper/Folia 1.21+, and <span class="inline-code">logs/latest.log</span>.</p></section>
        <section id="q2"><h2>Folia support?</h2><p>Yes, it is marked Folia-safe.</p></section>
        <section id="q3"><h2>PlaceholderAPI not resolving?</h2><p>Install PlaceholderAPI and test as a player sender.</p></section>
      `
    },

    changelog: {
      title: 'Changelog · EzDonutStore Docs',
      topbarTitle: 'Changelog',
      meta: ['More', 'Releases'],
      content: `
        <h1>Changelog</h1>

        <section id="latest"><h2>v1.0.0 - Latest</h2>
          <p><strong>Released:</strong> TBD</p>
          <ul>
            <li>Initial EzDonutStore release for Paper and Folia 1.21+</li>
            <li><span class="inline-code">/store</span> and <span class="inline-code">/buy</span> commands</li>
            <li><span class="inline-code">/store reload</span> and <span class="inline-code">/buy reload</span></li>
            <li>Optional PlaceholderAPI support and EzDonutStore placeholders</li>
          </ul>
        </section>
      `
    }
  }
};
