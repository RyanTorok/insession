//Main.java

private static class SideBar extends VBox {
    private Main holder;
    
    private TranslateTransition init;
    private TranslateTransition in;
    private TranslateTransition out;
    
    private ArrayList<Menu> options;
    
    public SideBar(Main holder) {
        super();
        this.holder = holder;
        
        //initial placement
        init = new TranslateTransition();
        init.setTarget(this);
        init.setDurration(400);
        init.setByX(2100);
        init.play();
        
        //enter screen animation
        in = new TranslateTransition();
        in.setTarget(this);
        in.setDurration(400);
        in.setByX(-300);
        
        //exit screen animation
        out = new TranslateTransition();
        out.setTarget(this);
        out.setDurration(400);
        out.setByX(300);
        
        menus = new ArrayList()<>;
        
        Menu openTerminal = new Menu("Open Terminal");
        Menu grades = new Menu("My Grades");
        Menu attendance = new Menu("Attendance History");
        Menu accountSettings = new Menu("Account Settings");
        Menu kbShortcuts = new Menu("Keyboard Shortcuts");
        Menu history = new Menu("Usage History");
        Menu privacy = new Menu("Privacy Policy");
        Menu help = new Menu("Help");
        Menu switch = new Menu("Switch User");
        Menu switch = new Menu("Save and Exit");
        
        
        menus.add(openTerminal);
        menus.add(grades);
        menus.add(attendance);
        menus.add(accountSettings);
        menus.add(kbShortcuts);
        menus.add(history);
        menus.add(privacy);
        menus.add(help);
        menus.add(switch);
        menus.add(quit);
        
        openTerminal.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            this.exit();
            term.start();
            state = TERMINAL_STATE;
        });
        
        grades.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            //TODO
        });
        
        attendance.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
             //TODO
        });
        
        accountSettings.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            //TODO
        });
        
        history.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            //TODO
        });
        
        privacy.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            //TODO
        });
        
        help.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            //TODO
        });
        
        switch.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Main.this.primaryStage.setMaximized(false);
            Main.this.newUser();
        });
        
        quit.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Main.this.stop();
        });
    }
    
    public void enter() {
        in.play();
    }
    
    public void disappear() {
        out.play();
    }
    
    private static class Menu extends AnchorPane {
        
        Text text;
        
        public Menu (String text) {
            setStyle("-fx-background-color: #505050");
            setPadding(new Insets(10);
            VBox.setHGap(this, Priority.ALWAYS);
            Text prompt = new Text(text);
            prompt.setFill(Color.WHITE);
            prompt.setFont(Font.font("Comfortaa", FontWeight.NORMAL, 20));
            this.getChildren().add(prompt);
            this.text = prompt;
            setAlignment(Alignment.CENTER);
        }
                       
        public void setText(String txt) {
            text.setText(txt);
            
        }
                      
        public Text getText() {return text;}
    }
}

/*
public void switchToMain() {
//...
SideBar sideBar = new SideBar(this);
mainBody.getChildren().add(sideBar);
mainBody.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
    if (event.getTarget() != sideBar && state == SIDEBAR_STATE) {
        sideBar.exit();
        state = BASE_STATE;
    }
});

picture.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
    sideBar.enter();
});

//main keyevent (escape)
//...
if (state == SIDEBAR_STATE)
    sideBar.exit();
//...

//...
}
*/

