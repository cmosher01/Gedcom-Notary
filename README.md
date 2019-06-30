# Gedcom-Notary

Copyright © 2017, Christopher Alan Mosher, Shelton, Connecticut, USA, <cmosher01@gmail.com>.

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=CVSSQ2BWDCKQ2)
[![License](https://img.shields.io/github/license/cmosher01/Gedcom-Notary.svg)](https://www.gnu.org/licenses/gpl.html)

When you import a GEDCOM file into an application, and then export it,
many times some of the lines are lost. You can use this program to help
preserve them, so they survive a round trip.

First, run this program to "hide" the tags into NOTE lines (which
usually survive). Then after a round trip import and export, re-run
this program to "extract" the tags out of the NOTE lines.
