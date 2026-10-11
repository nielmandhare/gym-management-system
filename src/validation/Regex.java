/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package validation;

/**
 *
 * @author mgssr
 */
public class Regex {

    public enum Validation {
        EMAIL() {
            @Override
            public String validate() {
                return "^[a-zA-Z0-9_!#$%&amp;'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
            }
        },
        USERNAME() {
            @Override
            public String validate() {
                return "^[a-zA-Z0-9](?!.*[_.]{2})[a-zA-Z0-9._]{1,18}[a-zA-Z0-9]$";
            }
        },
        MOBILE() {
            @Override
            public String validate() {
                return "^(?:(?:0)(?:7)(?:[01245678])(?:[0-9]{7})|[6-9][0-9]{9})$";
            }
        },
        NIC() {
            @Override
            public String validate() {
                return "^(?:\\d{9}[Vv]|\\d{12})$";
            }
        },
        PASSWORD() {
            @Override
            public String validate() {
                return "^.{3,128}$";
            }
        };

        public String validate() {
            return "";
        }
    }
}
