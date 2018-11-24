README
======

Example which demonstrates the stripping of invalid XML characters when using the Woodstox XML parser.

This is used to demonstrate a workaround to the problem described at https://github.com/FasterXML/woodstox/issues/37.

The workaround uses a filtered InputStreamReader to remove the problematic XML characters.

