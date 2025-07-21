package employees;

import org.hibernate.envers.RevisionListener;

public class StubUsernameListener implements RevisionListener {

    @Override
    public void newRevision(Object o) {
        if (o instanceof GlobalRevisionEntity revisionEntity) {
            revisionEntity.setUsername("stub-username");
        }
    }
}
