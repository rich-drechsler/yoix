sub peer_changepassword {
    #
    # The default version of this script works with the simple password file
    # that's provided with the default server, but it can easily be modified
    # to fit your own needs. If you customize this function you probably will
    # also want to change the validation function.
    #
    # This function should be called with three arguments
    #
    #    1: user id (should be set to $USER)
    #    2: old password
    #    3: new password
    #
    # and return 0 if the password was changed. Output is for the client side,
    # so we write the header and follow it by a word that indicates the status
    # of the password change operation, which should be "ok" if it succeeded.
    # Other values may be recognized, but "ok" is the only one that means the
    # user's password successfully was changed.
    # 

    my $status = "failed";

    #############################################################
    #
    # Replace the code below here (to the next comment) with
    # your application specific code.
    #
    #############################################################

    if (length($_[2]) >= 4) {
	($status) = ValidateUser($_[0], $_[1], $_[2]);
	sleep(2);	# make the user wait a bit
    }

    #############################################################
    #
    # End code replacement
    #
    #############################################################

    WriteHeader();
    print($status);	# answer for the client screen

    return($status eq "ok" ? 0 : 1);
}

return 1;
