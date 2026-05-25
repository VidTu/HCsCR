# Security Policy

## Vulnerabilities

### Reporting

HCsCR is a Minecraft mod. Proper security in this realm is a rare occasion.
Additionally, the niche/role of HCsCR isn't very prone to vulnerabilities.
However, if you think the bug you have found is a vulnerability,
you can report it privately via any of the following methods:

- GitHub Private vulnerability reporting: Head over to the
  [Security](https://github.com/VidTu/HCsCR/security)
  tab and click "*Report a vulnerability*".
- Mail: `imvidtu <at> proton <dot> me`

Where possible, **prefer** GitHub Private vulnerability reporting.

There are no public keys (e.g, PGP) to encrypt communication, sorry.[^1]

[^1]: PGP encryption is on my TODO/TBD list. If you really want
      to use it, you should use the same public key I sign my
      commits with. It is *not* uploaded to any public keyserver.

### Supported Versions

The only supported versions for vulnerability reporting are:

- The latest release published to GitHub.
- The latest pre-release published to GitHub[^2].
- The latest Git commit build.

[^2]: Pre-release versions are supported *only* if they
      were published after the latest *stable* release.

## Artifacts (Binaries/JARs)

### Reproducible Builds

HCsCR reproducibility status is currently unknown.

### Signing

HCsCR is not signed by digital signatures (namely PGP). Sorry![^3]

[^3]: PGP signing is on my TODO/TBD list.

### Supply Chain

HCsCR has implemented some supply chain validation, however,
**most of the supply chain isn't validated currently**.
