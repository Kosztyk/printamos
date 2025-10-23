Printing options:

```shell
$ lp --help
Usage: lp [options] [--] [file(s)]
       lp [options] -i id
Options:
-c                      Make a copy of the print file(s)
-d destination          Specify the destination
-E                      Encrypt the connection to the server
-h server[:port]        Connect to the named server and port
-H HH:MM                Hold the job until the specified UTC time
-H hold                 Hold the job until released/resumed
-H immediate            Print the job as soon as possible
-H restart              Reprint the job
-H resume               Resume a held job
-i id                   Specify an existing job ID to modify
-m                      Send an email notification when the job completes
-n num-copies           Specify the number of copies to print
-o option[=value]       Specify a printer-specific option
-o job-sheets=standard  Print a banner page with the job
-o media=size           Specify the media size to use
-o number-up=N          Specify that input pages should be printed N-up (1, 2, 4, 6, 9, and 16 are supported)
-o orientation-requested=N
                        Specify portrait (3) or landscape (4) orientation
-o print-quality=N      Specify the print quality - draft (3), normal (4), or best (5)
-o sides=one-sided      Specify 1-sided printing
-o sides=two-sided-long-edge
                        Specify 2-sided portrait printing
-o sides=two-sided-short-edge
                        Specify 2-sided landscape printing
-P page-list            Specify a list of pages to print
-q priority             Specify the priority from low (1) to high (100)
-s                      Be silent
-t title                Specify the job title
-U username             Specify the username to use for authentication

$ lpstat --help
Usage: lpstat [options]
Options:
-E                      Encrypt the connection to the server
-h server[:port]        Connect to the named server and port
-l                      Show verbose (long) output
-U username             Specify the username to use for authentication
-H                      Show the default server and port
-W completed            Show completed jobs
-W not-completed        Show pending jobs
-a [destination(s)]     Show the accepting state of destinations
-c [class(es)]          Show classes and their member printers
-d                      Show the default destination
-e                      Show available destinations on the network
-o [destination(s)]     Show jobs
-p [printer(s)]         Show the processing state of destinations
-r                      Show whether the CUPS server is running
-R                      Show the ranking of jobs
-s                      Show a status summary
-t                      Show all status information
-u [user(s)]            Show jobs queued by the current or specified users
-v [printer(s)]         Show the devices for each destination

```