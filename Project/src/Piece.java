import javax.swing.ImageIcon;

public class Piece
{
    private String type; 
    private ImageIcon icon;
    private boolean isAIControlled; 

    public Piece(String type, String iconPath, boolean isAIControlled) 
    
    {
        this.type = type;
        this.icon = new ImageIcon(iconPath);
        this.isAIControlled = isAIControlled;
    }

    public boolean isAIControlled() 
    {
        return isAIControlled;
    }

    public ImageIcon getIcon() 
    {
        return icon;
    }

    public String getType() 
    {
        return type;
    }
}
