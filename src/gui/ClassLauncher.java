package gui;

import classes.ClassPd;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import main.Root;

public class ClassLauncher extends VBox {

    public ClassLauncher(ClassPd classPd, int width) {
        super();
        Image graphic = classPd.getCastOf().getGraphic();
        Rectangle imgFiller = new Rectangle();
        if (graphic != null)
            getChildren().add(new ImageView(graphic));
        else {
            Color classColor = classPd.getColor();
            if (classColor != null)
                imgFiller.setFill(classColor);
            else {
                switch (classPd.getPeriodNo() % 7) {
                    case 1:
                        imgFiller.setFill(Color.RED);
                        break;
                    case 2:
                        imgFiller.setFill(Color.ORANGE);
                        break;
                    case 3:
                        imgFiller.setFill(Color.YELLOW);
                        break;
                    case 4:
                        imgFiller.setFill(Color.GREEN);
                        break;
                    case 5:
                        imgFiller.setFill(Color.BLUE);
                        break;
                    case 6:
                        imgFiller.setFill(Color.INDIGO);
                        break;
                    case 0:
                        imgFiller.setFill(Color.PURPLE);
                        break;
                    default:
                        imgFiller.setFill(Color.BLACK); //should never get this
                }
            }
            imgFiller.setHeight(2 * width / 3);
            imgFiller.setWidth(width);
            getChildren().add(imgFiller);
        }

        VBox classHeader = new VBox();
        classHeader.setStyle("-fx-background-color: #c5c5c5");
        classHeader.setPadding(new Insets(5));
        classHeader.setPrefWidth(width);
        classHeader.setSpacing(8);

        Text classTitle = new Text(classPd.getCastOf().getName() + " - P" + classPd.getPeriodNo());
        classTitle.setFont(Font.font("Sans Serif", FontWeight.BOLD, 20));
        classHeader.getChildren().add(classTitle);
        Text teacher = new Text(classPd.getTeacherFirst() + " " + classPd.getTeacherLast());
        teacher.setFont(Font.font("Sans Serif", FontPosture.ITALIC, 20));
        classHeader.getChildren().add(teacher);
        addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            classTitle.setUnderline(true);
            teacher.setUnderline(true);
        });
        addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            classTitle.setUnderline(false);
            teacher.setUnderline(false);
        });
        addEventHandler(MouseEvent.MOUSE_CLICKED, event -> Root.getPortal().launchClass(classPd));
        getChildren().add(classHeader);
    }
}
