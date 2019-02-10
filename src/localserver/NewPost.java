package localserver;

import localserver.database.DatabaseUtils;
import localserver.database.QueryGate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class NewPost extends Command {

    public NewPost(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException, SQLException {
        UUID postID = IDAllocator.get();
        Long userID = getExecutorId();
        UUID classItemID = optionalArgumentAsUUID(0);
        String title = getArgumentAsString(1),
                unformattedText = getArgumentAsString(2),
                styles = getArgumentAsString(3);
        Long visibility = getArgumentAsLong(4),
                nameVisibility = getArgumentAsLong(5);
        UUID parentPostID = optionalArgumentAsUUID(6);
        String type = getArgumentAsString(7);
        Boolean pinned = getArgumentAsBoolean(8);
        UUID previousVersion = optionalArgumentAsUUID(9);

        QueryGate gate = new QueryGate();

        //check to make sure it's the same user if there's a previous version, and that the post marked as the previous isn't in the middle of a chain
        if (previousVersion != null) {
            ResultSet previousPoster = gate.query("SELECT poster FROM posts WHERE uuid = ?", "u", previousVersion);
            while (previousPoster.isBeforeFirst())
                previousPoster.next();
            if (previousPoster.getLong("poster") != getExecutorId()) return "error : unauthorized chain";

            ResultSet countExistingChains = gate.query("SELECT COUNT(uuid) AS num FROM posts WHERE previous_version = ?", "u", postID);
            while (countExistingChains.isBeforeFirst())
                countExistingChains.next();
            if (countExistingChains.getInt("num") != 0) return "error : illegal chaining";
        }

        gate.update("INSERT INTO posts (uuid, poster, class_item, title, unformatted_text, styles, visibility, name_visibility, parent, type, pinned, previous_version) VALUES " + DatabaseUtils.questionMarks(12, true) + ";",
                "ulusssssusiu",
                postID, userID, classItemID, title, unformattedText, styles, parseVisibility(visibility), parseNameVisibility(nameVisibility), parentPostID, type, pinned ? 1 : 0, previousVersion);

        return postID.toString();
    }

    private String parseVisibility(Long type) {
        switch (type.intValue()) {
            case 0: return "public";
            case -1: return "private";
            default: return "group";
        }
    }

    private String parseNameVisibility(Long type) {
        switch (type.intValue()) {
            case 0: return "all";
            case -1: return "instructors";
            case -2: return "none";
            default: return "group";
        }
    }

    private String parseType(Integer type) {
        switch (type) {
            case 0: return "question";
            case 1: return "note";
            case 2: return "answer";
            case 3: return "comment";
            default: return "note";
        }
    }
}
