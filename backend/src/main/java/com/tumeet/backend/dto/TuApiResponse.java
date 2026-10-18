package com.tumeet.backend.dto;

public class TuApiResponse {

    private long timestamp;
    private boolean status;
    private String message;
    private StudentData data;

    public TuApiResponse() {
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public StudentData getData() {
        return data;
    }

    public void setData(StudentData data) {
        this.data = data;
    }

    public static class StudentData {
        private String type;
        private String statusname;
        private String userName;
        private String prefixname;
        private String displayname_th;
        private String displayname_en;
        private String email;
        private String faculty;
        private String department;

        public StudentData() {
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStatusname() {
            return statusname;
        }

        public void setStatusname(String statusname) {
            this.statusname = statusname;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPrefixname() {
            return prefixname;
        }

        public void setPrefixname(String prefixname) {
            this.prefixname = prefixname;
        }

        public String getDisplayname_th() {
            return displayname_th;
        }

        public void setDisplayname_th(String displayname_th) {
            this.displayname_th = displayname_th;
        }

        public String getDisplayname_en() {
            return displayname_en;
        }

        public void setDisplayname_en(String displayname_en) {
            this.displayname_en = displayname_en;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFaculty() {
            return faculty;
        }

        public void setFaculty(String faculty) {
            this.faculty = faculty;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }
    }
}
