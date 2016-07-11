/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rvme.controller;

import audio_manager.AudioManager;
import java.io.File;
import java.util.ArrayList;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.FileChooser;
import properties_manager.PropertiesManager;
import static rvme.PropertyType.ADD_ERROR_MESSAGE;
import static rvme.PropertyType.ADD_ERROR_TITLE;
import static rvme.PropertyType.ADD_TITLE;
import static rvme.PropertyType.IMAGE_EXT_DESC;
import static rvme.PropertyType.JPG_EXT;
import static rvme.PropertyType.PNG_EXT;
import rvme.data.DataManager;
import rvme.data.SubRegion;
import rvme.gui.SubRegionDialogSingleton;
import rvme.gui.Workspace;
import saf.AppTemplate;
import static saf.settings.AppStartupConstants.FILE_PROTOCOL;
import static saf.settings.AppStartupConstants.PATH_IMAGES;

/**
 *
 * @author Jon Reyes
 */
public class RVMEController {
    AppTemplate app;
    PropertiesManager props;
    AudioManager anthem;
    
    public RVMEController(AppTemplate initApp){
        app = initApp;
        props = PropertiesManager.getPropertiesManager();
    }
    
    public void updateBGColor(){
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        DataManager dataManager = (DataManager) app.getDataComponent();
        
        Color bgColor = workspace.getBGCPicker().getValue();
        dataManager.setBackgroundColor(bgColor);
        workspace.updateFileControls(false,false);
    }
    
    public void updateBorderColor(){
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        DataManager dataManager = (DataManager) app.getDataComponent();
        
        Color borderColor = workspace.getBCPicker().getValue();
        dataManager.setBorderColor(borderColor);
        workspace.updateFileControls(false,false);
    }
    
    public void updateBorderThickness(){
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        DataManager dataManager = (DataManager) app.getDataComponent();
        
        double borderThickness = workspace.getBTSlider().getValue();
        dataManager.setBorderThickness(borderThickness);
        
        workspace.getBTValue().setText(String.format("%.2f", borderThickness));
        workspace.updateFileControls(false,false);
    }
    
    public void updateZoom(){
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        DataManager dataManager = (DataManager) app.getDataComponent();
        
        double zoom = workspace.getZoomSlider().getValue();
        dataManager.setZoom(zoom);
        
        workspace.getZoomValue().setText(String.format("%.2fx", zoom));
        
        StackPane mapStack = workspace.getMapStack();
        for(Node n: mapStack.getChildren()){
            if(n instanceof Group || n instanceof ImageView){
                n.setScaleX(zoom);
                n.setScaleY(zoom);
            }
        }
        
        workspace.updateFileControls(false,false);
    }
    
    public void editSubRegion(MouseEvent e){
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        SubRegionDialogSingleton subRegionDialog = workspace.getSubRegionDialog();
        
        subRegionDialog.update(e);
        subRegionDialog.show();
    }
    
    public void updateTableData(){
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        DataManager dataManager = (DataManager) app.getDataComponent();
        
        ObservableList<SubRegion> mapData = workspace.getMapData();
        dataManager.setTableItems(mapData);
        workspace.updateFileControls(false,false);
    }
    
    public void addImage(){
        try{
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File(PATH_IMAGES));
            fc.setTitle(props.getProperty(ADD_TITLE));
            fc.getExtensionFilters().addAll(
		new FileChooser.ExtensionFilter(props.getProperty(IMAGE_EXT_DESC), props.getProperty(PNG_EXT), props.getProperty(JPG_EXT)));
            File selectedImage = fc.showOpenDialog(app.getGUI().getWindow());
            if (selectedImage != null) {
                add(selectedImage);
            }
        }catch (Exception e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText(props.getProperty(ADD_ERROR_TITLE));
                alert.setContentText(props.getProperty(ADD_ERROR_MESSAGE));
                alert.showAndWait();
        }
    }
    
    public void add(File selectedImage){
        String imagePath = FILE_PROTOCOL + selectedImage.getPath();
        Image image = new Image(imagePath);
        ImageView addImageView = new ImageView(image);
        addImageView.setPreserveRatio(true);
        addImageView.setFitWidth(200);
        addImageView.setOnMouseClicked(e->{
            if(e.getClickCount()==2){
                selectImage(addImageView);
            }
        });
                
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        DataManager dataManager = (DataManager) app.getDataComponent();
                
        StackPane mapStack = workspace.getMapStack();
        mapStack.getChildren().add(addImageView);
        workspace.updateFileControls(false,false);
        //workspace.reloadWorkspace();
    }
    
    public void removeImage(){
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        StackPane mapStack = workspace.getMapStack();
        mapStack.getChildren().remove(workspace.getSelection());
        workspace.updateFileControls(false,false);
    }
    
    private void selectImage(ImageView imageView){
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        workspace.setSelection(imageView);
        workspace.updateEditControls();
    }
    
    public void playAnthem(){
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        workspace.getPlayButton().setVisible(false);
        workspace.getPauseButton().setVisible(true);
        
        DataManager data = (DataManager) app.getDataComponent();
        anthem = new AudioManager();
        try {
            anthem.loadAudio(data.getName(), data.getAnthem());
        } catch (Exception e){
        }
        anthem.play(data.getName(), true);
    }
    
    public void pauseAnthem(){
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        workspace.getPauseButton().setVisible(false);
        workspace.getPlayButton().setVisible(true);
        
        DataManager data = (DataManager) app.getDataComponent();
        anthem.stop(data.getName());
    }
    
    public void reassignColors(){
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        DataManager dataManager = (DataManager) app.getDataComponent();
        
        ArrayList<Color> randomColors = dataManager.randomColors();
        int i = 0;
        for(Node node: workspace.getRegion().getChildren()){
            if (node instanceof Polygon){
                ((Polygon) node).setFill(randomColors.get(i));
                i++;
            }
        }
        dataManager.setMapColors(randomColors);
        
        workspace.updateFileControls(false,false);
    }
}
