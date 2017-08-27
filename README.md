# Gedcom-Notary

When you import a GEDCOM file into an application, and then export it,
many time some of the lines are lost. You can use this program to help
preserve them, so they survive a round trip.

First, run this program to "hide" the tags into NOTE lines (which
usually survive). Then after a round trip import and export, re-run
this program to "extract" the tags out of the NOTE lines.
