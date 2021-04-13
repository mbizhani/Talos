printf "\n---- IP ----\n"
ip a

printf "\n\n---- ROUTING -----\n"
ip r

printf "\n\n---- TCP SOCKETS -----\n"
ss -lntp

printf "\n\n---- UDP SOCKETS -----\n"
ss -lnup