package server;

import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;

import javax.swing.*;
import javax.swing.GroupLayout.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;

import common.*;
import server.ServerInterface.UnableToDeleteException;

/**
 * Provides the Sushi Server user interface
 * You should not need to modify this class if your implementation of the interface is correct.
 *
 */
public class ServerWindow extends JFrame implements UpdateListener {

	private static final long serialVersionUID = -7033665524039475019L;
	private ServerInterface server;

	//Set up panels
	private DualPanel<Order> orderPanel;
	private DualPanel<Dish> dishPanel;
	private DualPanel<Ingredient> ingredientPanel;
	private DualPanel<Supplier> supplierPanel;
	private DualPanel<Staff> staffPanel;
	private DualPanel<Drone> dronePanel;
	private DualPanel<User> userPanel;
	private DualPanel<Postcode> postcodePanel;
	private ConfigurationPanel configurationPanel;
	
	/**
	 * Create a new server window with all associated panels
	 * @param server
	 */
	public ServerWindow(ServerInterface server) {
		super("Sushi Server");
		this.server = server;
		server.addUpdateListener(this);
				
		//Set up panels
		orderPanel = new DualPanel<Order>(Order.class,() -> server.getOrders());
		dishPanel = new DualPanel<Dish>(Dish.class,() -> server.getDishes());
		ingredientPanel = new DualPanel<Ingredient>(Ingredient.class,() -> server.getIngredients());
		supplierPanel = new DualPanel<Supplier>(Supplier.class,() -> server.getSuppliers());
		staffPanel = new DualPanel<Staff>(Staff.class,() -> server.getStaff());
		dronePanel = new DualPanel<Drone>(Drone.class,() -> server.getDrones());
		userPanel = new DualPanel<User>(User.class,() -> server.getUsers());
		postcodePanel = new DualPanel<Postcode>(Postcode.class,() -> server.getPostcodes());
		configurationPanel = new ConfigurationPanel();
		
		//Set up tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Orders",orderPanel);
        tabs.addTab("Dishes",dishPanel);
        tabs.addTab("Ingredients",ingredientPanel);
        tabs.addTab("Suppliers",supplierPanel);
        tabs.addTab("Staff",staffPanel);
        tabs.addTab("Drones",dronePanel);
        tabs.addTab("Users",userPanel);
        tabs.addTab("Postcodes",postcodePanel);
        tabs.addTab("Configuration", configurationPanel);
        add(tabs);
        
        //Set up properties and actions
        setupProperties();
        setupActions();
		
		//Display window
		setSize(800,600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
		//Start timed updates
		startTimer();
	}
	
	/**
	 * Start the timer which updates the user interface based on the given interval to update all panels
	 */
	public void startTimer() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);     
        int timeInterval = 5;
        
        scheduler.scheduleAtFixedRate(() -> refreshAll(), 0, timeInterval, TimeUnit.SECONDS);
	}
		
	/**
	 * Set up the explicit properties that will be displayed and edited in the user interface.
	 * Any public getter() will also be added automatically.
	 */
	private void setupProperties() {
		//Orders
		orderPanel.addColumn("Cost", order -> server.getOrderCost((Order) order));
		orderPanel.addColumn("Status", order -> server.getOrderStatus((Order) order));

		//Dishes
		dishPanel.addProperty("Name");
		dishPanel.addProperty("Description");
		dishPanel.addProperty("Price","Number");
		dishPanel.addProperty("RestockThreshold","Number");
		dishPanel.addProperty("RestockAmount","Number");
		dishPanel.addColumn("Stock",() -> server.getDishStockLevels());

		//Ingredients
		ingredientPanel.addProperty("Name");
		ingredientPanel.addProperty("Unit");
		ingredientPanel.addProperty("Supplier",() -> server.getSuppliers());
		ingredientPanel.addProperty("RestockThreshold","Number");
		ingredientPanel.addProperty("RestockAmount","Number");
		ingredientPanel.addColumn("Stock",() -> server.getIngredientStockLevels());
		
		//Staff
		staffPanel.addProperty("Name");
		staffPanel.addColumn("Status", staff -> server.getStaffStatus((Staff) staff));
		
		//Suppliers
		supplierPanel.addProperty("Name");
		supplierPanel.addProperty("Distance","Number");
		
		//Drones
		dronePanel.addProperty("Speed","Number");
		dronePanel.addColumn("Status", drone -> server.getDroneStatus((Drone) drone));
		
		//Postcodes
		postcodePanel.addProperty("Code");
		postcodePanel.addProperty("Distance","Number");
	}

	/**
	 * Setup the actions linked to the buttons in the user interface
	 */
	private void setupActions() {
		//Orders Panel
		orderPanel.add.setText("Remove complete");
		orderPanel.add.addActionListener(e -> {
			List<Order> orders = server.getOrders();
			ArrayList<Order> toDelete = new ArrayList<Order>();
			for(Order order : orders) {
				toDelete.add(order);
			}
			for(Order order : toDelete) {
				if(server.isOrderComplete(order)) {
					try {
						server.removeOrder(order);
					} catch (UnableToDeleteException exception) {
						JOptionPane.showMessageDialog(orderPanel, "Unable to remove order: " + exception.getMessage());
					}
				}
			}
		});
		orderPanel.delete.addActionListener(e -> {
				Order currentOrder = orderPanel.getActive();
				try {
					server.removeOrder(currentOrder);
				} catch (UnableToDeleteException exception) {
					JOptionPane.showMessageDialog(orderPanel, "Unable to remove order: " + exception.getMessage());
				}
		});
		
		//Dishes
		dishPanel.results.linkEnabled(dishPanel.edit);	
		dishPanel.add.addActionListener(e -> {
			String name = (String) dishPanel.get("Name");
			String unit = (String) dishPanel.get("Description");
			Number price = (Number) dishPanel.get("Price");
			Number restockThreshold = (Number) ingredientPanel.get("RestockThreshold");
			Number restockAmount = (Number) ingredientPanel.get("RestockAmount");
			Dish newDish = server.addDish(name,unit,price,restockThreshold,restockAmount);
			dishPanel.refresh();
			dishPanel.reset();
			editRecipe(newDish);
		});
		dishPanel.edit.addActionListener(e -> {
			Dish currentDish = dishPanel.getActive();
			editRecipe(currentDish);
		});
		dishPanel.delete.addActionListener(e -> {
			Dish currentDish = dishPanel.getActive();
			try {
				server.removeDish(currentDish);
			} catch (UnableToDeleteException exception) {
				JOptionPane.showMessageDialog(orderPanel, "Unable to remove dish: " + exception.getMessage());
			}
		});
		
		//Ingredients Panel
		ingredientPanel.results.linkEnabled(ingredientPanel.edit);	
		ingredientPanel.edit.addActionListener(e -> {
			Ingredient currentIngredient = ingredientPanel.getActive();
			editIngredient(currentIngredient);
		});
		ingredientPanel.add.addActionListener(e -> {
				String name = (String) ingredientPanel.get("Name");
				String unit = (String) ingredientPanel.get("Unit");
				Supplier supplier = (Supplier) ingredientPanel.get("Supplier");
				Number restockThreshold = (Number) ingredientPanel.get("RestockThreshold");
				Number restockAmount = (Number) ingredientPanel.get("RestockAmount");
				server.addIngredient(name, unit, supplier, restockThreshold, restockAmount);
				ingredientPanel.refresh();
				ingredientPanel.reset();
		});
		ingredientPanel.delete.addActionListener(e -> {
				Ingredient currentIngredient = ingredientPanel.getActive();
				try {
					server.removeIngredient(currentIngredient);
				} catch (UnableToDeleteException exception) {
					JOptionPane.showMessageDialog(orderPanel, "Unable to remove ingredient: " + exception.getMessage());
				}
		});
		
		//Suppliers Panel
		supplierPanel.add.addActionListener(e -> {
				String name = (String) supplierPanel.get("Name");
				Number distance = (Number) supplierPanel.get("Distance");
				server.addSupplier(name,distance);
				supplierPanel.reset();
		});
		supplierPanel.delete.addActionListener(e -> {
				Supplier currentSupplier = supplierPanel.getActive();
				try {
					server.removeSupplier(currentSupplier);
				} catch (UnableToDeleteException exception) {
					JOptionPane.showMessageDialog(orderPanel, "Unable to remove supplier: " + exception.getMessage());
				}
		});
		
		//Staff Panel
		staffPanel.add.addActionListener(e -> {
				String name = (String) staffPanel.get("Name");
				server.addStaff(name);
				staffPanel.reset();
		});
		staffPanel.delete.addActionListener(e -> {
				Staff currentStaff = staffPanel.getActive();
				try {
					server.removeStaff(currentStaff);
				} catch (UnableToDeleteException exception) {
					JOptionPane.showMessageDialog(orderPanel, "Unable to remove staff: " + exception.getMessage());
				}
		});
		
		//Drones Panel
		dronePanel.add.addActionListener(e -> {
				Number speed = (Number) dronePanel.get("Speed");
				server.addDrone(speed);
				dronePanel.reset();
		});
		dronePanel.delete.addActionListener(e -> {
				Drone currentDrone = dronePanel.getActive();
				try {
					server.removeDrone(currentDrone);
				} catch (UnableToDeleteException exception) {
					JOptionPane.showMessageDialog(orderPanel, "Unable to remove drone: " + exception.getMessage());
				}
		});
		
		//Postcodes Panel
		postcodePanel.add.addActionListener(e -> {
				String code = (String) postcodePanel.get("Code");
				Number distance = (Number) postcodePanel.get("Distance");
				server.addPostcode(code,distance);
				postcodePanel.reset();
		});
		postcodePanel.delete.addActionListener(e-> {
				Postcode currentPostcode = postcodePanel.getActive();
				try {
					server.removePostcode(currentPostcode);
				} catch (UnableToDeleteException exception) {
					JOptionPane.showMessageDialog(orderPanel, "Unable to remove postcode: " + exception.getMessage());
				}
		});
		
		//Users panel
		userPanel.add.setEnabled(false);
		userPanel.delete.addActionListener(e -> {
				User currentUser = userPanel.getActive();
				try {
					server.removeUser(currentUser);
				} catch (UnableToDeleteException exception) {
					JOptionPane.showMessageDialog(orderPanel, "Unable to remove user: " + exception.getMessage());
				}
		});
	}
	
	/**
	 * Display the edit recipe window
	 * @param dish dish to edit
	 */
	public void editRecipe(Dish dish) {
		RecipeFrame recipeFrame = new RecipeFrame(server,dish);
		recipeFrame.setVisible(true);
	}
	
	/**
	 * Display the edit ingredient window
	 * @param ingredient ingredient to edit
	 */
	public void editIngredient(Ingredient ingredient) {
		IngredientFrame ingredientFrame = new IngredientFrame(server,ingredient);
		ingredientFrame.setVisible(true);
	}
	
	/**
	 * A dual panel display which shows data at the top and allows adding new models at the bottom
	 *
	 * @param <T> model
	 */
	public class DualPanel<T> extends JPanel {
		
		private static final long serialVersionUID = 794325868675992863L;
		private EditorPanel<T> editor;
		private ResultsPanel<T> results;
		private Map<String, JComponent> properties = new HashMap<String,JComponent>();
		private JButton add;
		private JButton delete;
		private Class<?> baseClass;
		private java.util.function.Supplier dataSource;
		private JButton edit;
		
		
		/**
		 * Create a new dual panel, based on the given glass and providing data based on the given data source
		 * @param baseClass model class based on
		 * @param dataSource data source supplier which provides the data to display
		 */
		public DualPanel(Class<?> baseClass, java.util.function.Supplier dataSource) {
			this.baseClass = baseClass;
			this.dataSource = dataSource;
			
			setLayout(new BorderLayout());
			
			//Add dual editor and results pane at the top
			JPanel inner = new JPanel(new GridLayout(2,1));
			add(inner,BorderLayout.CENTER);
			editor = new EditorPanel<T>();
			results = new ResultsPanel<T>(baseClass,dataSource);
			inner.add(results);
			inner.add(editor);	
			
			//Add delete and add buttons at the bottom
			JPanel bottom = new JPanel(new GridLayout(1,2));
			add(bottom,BorderLayout.SOUTH);
			delete = new JButton("Delete");
			edit = new JButton("Edit");
			add = new JButton("Add");
			delete.setEnabled(false);
			edit.setEnabled(false);
			bottom.add(delete);
			bottom.add(edit);
			bottom.add(add);		
			
			//Enable the delete button when an item is selected
			results.linkEnabled(delete);
		}

		/**
		 * Get the active object selected in the results
		 * @return
		 */
		public T getActive() {
			return results.getActive();
		}

		/**
		 * Refresh the results
		 */
		public void refresh() {
			results.refresh();
		}
	
		/**
		 * Reset the input editors in the adding part of the pane
		 */
		public void reset() {
			for(JComponent component : properties.values()) {
				if(component instanceof JTextField) {
					((JTextField) component).setText("");
				} else if (component instanceof JSpinner) {
					((JSpinner) component).setValue(0);
				} else if (component instanceof JComboBox) {
					((JComboBox) component).setSelectedIndex(0);
				}
			}
		}

		/**
		 * Look up a given property by name by pulling it from the associated component
		 * @param property property name to look up
		 * @return the value from the linked field
		 */
		public Object get(String property) {
			JComponent component = properties.get(property);
			if(component instanceof JTextField) {
				return ((JTextField) component).getText();
			} else if (component instanceof JSpinner) {
				return ((JSpinner) component).getValue();
			} else if (component instanceof JComboBox<?>) {
				return ((JComboBox<? extends Model>) component).getSelectedItem();
			}
			return null;
		}
		
		/**
		 * Get the field mapped to a given property name
		 * @param property
		 * @return
		 */
		public JComponent getProperty(String property) {
			return properties.get(property);
		}

		/**
		 * Add a new property, as a default text field, to the editor
		 * @param property
		 */
		public void addProperty(String property) {
			JComponent field = editor.addProperty(property);
			properties.put(property,field);
		}
		
		/**
		 * Add a new property, with the given type, to the editor
		 * @param property
		 * @param type
		 */
		public void addProperty(String property, String type) {
			JComponent field = editor.addProperty(property,type);
			properties.put(property,field);
		}
		
		/**
		 * Add a new property, with a combobox type with the given data source to populate it
		 * @param property
		 * @param dataSource
		 */
		public void addProperty(String property, java.util.function.Supplier dataSource) {
			JComponent field = editor.addProperty(property,dataSource);
			properties.put(property,field);
		}
		
		/**
		 * Add a new column to the viewer, provided by the given data source which is called per model
		 * @param name
		 * @param dataSource
		 */
		public void addColumn(String name, java.util.function.Supplier dataSource) {
			results.addColumn(name, dataSource);
		}
		
		/**
		 * Add a new column to the viewer, provided by the given data source mapping of models to objects
		 * @param name
		 * @param dataSource
		 */
		public void addColumn(String name, java.util.function.Function<Model, Object> dataSource) {
			results.addColumn(name, dataSource);
		}

 	}
	
	/**
	 * Editor panel which displays a set of captions and fields
	 *
	 * @param <T> model type
	 */
	public class EditorPanel<T> extends JPanel {
		
		private static final long serialVersionUID = 5627333332721249294L;
		private ParallelGroup groupLabels;
		private ParallelGroup groupFields;
		private SequentialGroup groupRows;
		private GroupLayout layout;

		public EditorPanel () {
			layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
	
			groupLabels = layout.createParallelGroup();
			groupFields = layout.createParallelGroup();
			groupRows = layout.createSequentialGroup();
			layout.setHorizontalGroup(layout.createSequentialGroup()
				    .addGroup(groupLabels)
				    .addGroup(groupFields));
			layout.setVerticalGroup(groupRows);
		}

		/**
		 * Add a new property by name to the editor
		 * @param name of property
		 * @return
		 */
		public JComponent addProperty(String name) {
			return addProperty(name,"text");
		}
		
		/**
		 * Add a new property by name and with the given type
		 * @param name of property
		 * @param type type
		 * @return
		 */
		public JComponent addProperty(String name, String type) {
			JLabel label = new JLabel(name);

			JComponent field;
			if(type.equals("Number")) {
				SpinnerNumberModel minMax = new SpinnerNumberModel(0,0,100000,1);
				field = new JSpinner(minMax);
			} else {
				field = new JTextField();
			}
			
			addField(label,field);
		    return field;
		}	
		
		/**
		 * Add a new property with a combobox filled by the given data source
		 * @param name name of property
		 * @param dataSource 
		 * @return
		 */
		public JComponent addProperty(String name, java.util.function.Supplier dataSource) {
			JLabel label = new JLabel(name);
			
			JComboBox<String> field = new JComboBox<String>();
			field.setModel(new ComboModel(dataSource));
			if(field.getModel().getSize() > 0) {
				field.setSelectedIndex(0);
			}
			addField(label,field);
		    return field;
		}
		
		/**
		 * Provides the model to link a combobox to a datasource given backend
		 *
		 */
		public class ComboModel extends AbstractListModel implements ComboBoxModel {

			private static final long serialVersionUID = 3885818221611239892L;
			private Object selected;
			private java.util.function.Supplier<List<? extends Model>> dataSource;
			
			public ComboModel(java.util.function.Supplier<List<? extends Model>> dataSource) {
				this.dataSource = dataSource;
			}

			@Override
			public int getSize() {
				return dataSource.get().size();
			}

			@Override
			public Object getElementAt(int index) {
				return dataSource.get().get(index);
			}

			@Override
			public void setSelectedItem(Object anItem) {
				this.selected = anItem;
			}

			@Override
			public Object getSelectedItem() {
				return this.selected;
			}
				
		}
		
		public void addField(JLabel label, JComponent field) {
			groupLabels.addComponent(label);
		    groupFields.addComponent(field);
		    groupRows.addGroup(layout.createParallelGroup()
		        .addComponent(label)
		        .addComponent(field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
		}
	}
	
	/**
	 * Display a panel of results using a JTable linked to a datasource and particular class
	 * Public getters of that model will become automatic visible columns.
	 *
	 * @param <T> model
	 */
	public class ResultsPanel<T> extends JPanel {
				
		private static final long serialVersionUID = 8851523650992070549L;
		private String[] columns;
		private JTable table = new JTable();
		
		//Handle Map functions
		private HashMap<String, java.util.function.Supplier<Map<? extends Model,Object>>> extras = 
				new HashMap<String, java.util.function.Supplier<Map<? extends Model,Object>>>();
		
		//Handle individual functions
		private HashMap<String, Function<Model, Object>> extras2 = 
				new HashMap<String, Function<Model, Object>>();
		
		private ResultsTable<Model> modelTable;
		private List<? extends Model> model;
		private java.util.function.Supplier<List<? extends Model>> dataSource;
		private Class<?> baseClass;

		public ResultsPanel(Class<?> baseClass, java.util.function.Supplier<List<? extends Model>> dataSource) {
			this.baseClass = baseClass;
			this.dataSource = dataSource; 
			
			//Set up initial columns
			setColumns();
			
			//Set up initial data
			this.modelTable = new ResultsTable(this.columns,dataSource);
			table.setModel(this.modelTable);
			
			//Add component
			setLayout(new BorderLayout());
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane pane = new JScrollPane(table);
			add(pane,BorderLayout.CENTER);
		}
		
		public void setColumns() {
			ArrayList<String> viewable = new ArrayList<String>();
			
			Method[] methods = baseClass.getDeclaredMethods();
			
			ArrayList<Class> displayable = new ArrayList<Class>();
			displayable.add(String.class);
			displayable.add(Number.class);
			displayable.add(Model.class);
			
			for(Method method : methods) {
				String name = method.getName();
				
				//Check if this is a displayable class
				boolean toDisplay = false;
				for(Class<?> displayClass : displayable) {
					Class<?> returnClass = method.getReturnType();
					Class<?> superClass = method.getReturnType().getSuperclass();
					if (returnClass.isAssignableFrom(displayClass) | 
							(superClass != null && superClass.isAssignableFrom(displayClass))) {
						toDisplay = true;
						break;
					}
				}
				
				//Skip those we can't display
				if(!toDisplay) {
					continue;
				}
				//Ignore getName
				if(name.equals("getName")) {
					continue;
				}
				//If there is a getter, then add it to the list
				if(name.matches("^get.*")) {
					viewable.add(name.replaceFirst("get", ""));
				}
			}
			
			//Organise columns by alphabetical order, apart from name
			viewable.sort(null);
			
			//Add name to the start
			viewable.add(0, "Name");
			
			//Set up initial columns
			columns = viewable.toArray(new String[viewable.size()]);
			
		}
		
		/**
		 * Add a new viewer column with the given name
		 * @param name property name
		 * @return true if the column is new
		 */
		private boolean addColumn(String name) {
			//Dont allow duplicate columns
			if(Arrays.asList(columns).contains(name)) return false;
			
			columns = Arrays.copyOf(columns, columns.length + 1);
			columns[columns.length-1] = name;
			return true;
		}
		
		/**
		 * Add a new viewer column backed by the given data source which provides a list of all models to values
		 * @param name property name
		 * @param dataSource function to call which returns all models -> value pairs
		 */
		public void addColumn(String name, java.util.function.Supplier<Map<? extends Model,Object>> dataSource) {	
			if (addColumn(name)) {
				extras.put(name,dataSource);
				modelTable.setColumns(columns,extras,extras2);
				refresh();
			}
		}
		
		/**
		 * Add a new viewer column backed by the given data source which provides a function to call for each model
		 * @param name property name
		 * @param dataSource function to call which returns the value to display
		 */
		public void addColumn(String name, java.util.function.Function<Model,Object> dataSource) {
			if (addColumn(name)) {
				extras2.put(name,dataSource);
				modelTable.setColumns(columns,extras,extras2);
				refresh();
			}
		}

		/**
		 * Link a button to whether there is a selection selected
		 * @param button button to link
		 */
		public void linkEnabled(JButton button) {
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					button.setEnabled(table.getSelectedRow() >= 0);
				}
			});
		}

		/**
		 * Get the actively selected row in the table as its appropriate model
		 * @return
		 */
		public T getActive() {
			int index = table.getSelectedRow();
			if(index < 0) return null;
			return (T) modelTable.getValue(index);
		}
		
		/**
		 * Refresh all items in the table based on the linked data sources
		 */
		public void refresh() {
			//Store previous selection
			int previous = table.getSelectedRow();
			
			//Update the model
			this.modelTable.refresh();		
			
			//Reselect
			if(previous >= 0 && previous < table.getRowCount()) {
				table.setRowSelectionInterval(previous, previous);
			}
		}
		
	}
	
	/**
	 * Provides the results model to drive a JTable based on the provided data sources
	 *
	 * @param <T>
	 */
	public class ResultsTable<T> extends AbstractTableModel {
		
		private static final long serialVersionUID = -7827706350284097800L;
		private String[] columns;
		private Object[][] data;
		private HashMap<String, java.util.function.Supplier<Map<? extends Model, Object>>> extras = 
				new HashMap<String, java.util.function.Supplier<Map<? extends Model, Object>>>();
		private HashMap<String, Function<Model, Object>> extras2 = new HashMap<String, Function<Model, Object>>();

		private java.util.function.Supplier<List<? extends Model>> dataSource;
		private List<? extends Model> list;
		
		/**
		 * Create a new results table model with the given columns and extra properties
		 * @param columns string list of column names, which links to getters or extra properties
		 * @param dataSource data source to provide column rows for model
		 */
		public ResultsTable(String[] columns, java.util.function.Supplier<List<? extends Model>> dataSource) {
			this.columns = columns;
			this.dataSource = dataSource;
			
			list = dataSource.get();
			refresh();
		}

		/**
		 * Set the columns, providing extra properties as needed
		 * @param columns String list of column names which map to getters or extra properties
		 * @param extras mapping of property names to suppliers which return all model->value mappings
		 * @param extras2 mapping of property names to a function of model->value individually
		 */
		public void setColumns(String[] columns, 
				HashMap<String, java.util.function.Supplier<Map<? extends Model, Object>>> extras, 
				HashMap<String, Function<Model, Object>> extras2
				) {
			this.columns = columns;
			this.extras = extras;
			this.extras2 = extras2;
			this.fireTableStructureChanged();
			
			refresh();
		}

		/**
		 * Refresh all data in the table based on the data sources
		 */
		public void refresh() {
			list = dataSource.get();
			
			Object[][] data = new Object[list.size()][columns.length];
			int index = 0;
			for(Model model : list) {
				data[index] = new Object[columns.length];
				
				int col = 0;
				for(String columnName : columns) {
					//Check for Model->Value mapping in extras
					if(extras.containsKey(columnName)) {
						java.util.function.Supplier<Map<? extends Model, Object>> extraSource = extras.get(columnName);
						Map<? extends Model, Object> extraColumns = extraSource.get();
						data[index][col] = extraColumns.get(model);
					//Check for Model->Value function in extras2 to call on each model
					} else if(extras2.containsKey(columnName)) {
						Function<Model, Object> extraSource = extras2.get(columnName);
						data[index][col] = extraSource.apply(model);
					//Otherwise, call get<ColumName> on the model
					} else {
						try {
							Method method = model.getClass().getDeclaredMethod("get" + columnName);
							Object result = method.invoke(model, new Object[] {});
							if(result instanceof Model) {
								data[index][col] = ((Model) result).getName();
							} else {
								data[index][col] = result;
							}
						} catch (Exception e) {
							System.err.println("Unable to call get" + columnName + "() on " + model.getClass().getName());
							e.printStackTrace();
						}
					}
					
					//Move on to the next column
					col++;
				}
				
				//Move on to the next row
				index++;
			}
			this.data = data;
			this.fireTableDataChanged();
		}
		
		@Override
		 public String getColumnName(int columnIndex) {
			return columns[columnIndex];
		}
		
		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}
		
		public Object getValue(int index) {
			return list.get(index);
		}
	}
	
	/**
	 * Recipe editor frame, which allows editing the recipe components and restocking amounts
	 *
	 */
	public class RecipeFrame extends JFrame {

		private static final long serialVersionUID = 3617824143704795063L;
		private ServerInterface server;
		private Dish dish;
		private HashMap<Ingredient,JSpinner> ingredientMap = new HashMap<Ingredient,JSpinner>();

		/**
		 * Create a new recipe frame with the given server backend and for the given dish
		 * @param server backend to work with
		 * @param dish dish to edit recipe for
		 */
		public RecipeFrame(ServerInterface server, Dish dish) {
			super("Edit recipe: " + dish.getName());
			
			setSize(400,500);
			setLocationRelativeTo(null);
			
			this.server = server;
			this.dish = dish;
			setLayout(new BorderLayout());
			
			Map<Ingredient,Number> recipe = server.getRecipe(dish);
			IngredientList ingredientList = new IngredientList(recipe);
			JScrollPane scroller = new JScrollPane(ingredientList);
			add(scroller,BorderLayout.CENTER);
			
			JPanel buttons = new JPanel(new GridLayout(1,2));
			JButton save = new JButton("Save");
			JButton cancel = new JButton("Cancel");
			buttons.add(cancel);
			
			//Save recipe and restocking levels
			buttons.add(save);
			save.addActionListener(e -> {
				server.setRecipe(dish, getRecipe());
				server.setRestockLevels(dish, ingredientList.getRestockThreshold(), ingredientList.getRestockAmount());
				this.dispose();
			});
			cancel.addActionListener(e -> {
				this.dispose();
			});
			add(buttons,BorderLayout.SOUTH);
			
			//Set the save button as the default button
			JRootPane rootPane = SwingUtilities.getRootPane(this);
			rootPane.setDefaultButton(save);
		}
		
		/**
		 * Get the resulting recipe from the user interface as a mapping
		 * @return map of ingredient to quantity numbers forming the recipe
		 */
		public Map<Ingredient, Number> getRecipe() {
			HashMap<Ingredient,Number> recipe = new HashMap<Ingredient,Number>();
			for(Entry<Ingredient, JSpinner> mapping : ingredientMap.entrySet()) {
				Ingredient ingredient = mapping.getKey();
				Number value = (Number) mapping.getValue().getValue();
					if(value.intValue() > 0) {
						recipe.put(ingredient, value);
				}
			}
			return recipe;
		}
		
		/**
		 * Provide a list of all ingredients to support the recipe editor
		 *
		 */
		public class IngredientList extends JPanel {
			
			private static final long serialVersionUID = -534578199178503594L;
			private ParallelGroup groupLabels;
			private ParallelGroup groupFields;
			private SequentialGroup groupRows;
			private GroupLayout layout;
			private JSpinner restockThreshold;
			private JSpinner restockAmount;
			
			/**
			 * Create a new ingredient list, taking the current recipe to edit
			 * @param recipe
			 */
			public IngredientList(Map<Ingredient,Number> recipe) {
				//Setup layout
				layout = new GroupLayout(this);
				setLayout(layout);
				layout.setAutoCreateContainerGaps(true);
				layout.setAutoCreateGaps(true);
		
				groupLabels = layout.createParallelGroup();
				groupFields = layout.createParallelGroup();
				groupRows = layout.createSequentialGroup();
				layout.setHorizontalGroup(layout.createSequentialGroup()
					    .addGroup(groupLabels)
					    .addGroup(groupFields));
				layout.setVerticalGroup(groupRows);
				
				List<Ingredient> ingredients = server.getIngredients();
				for(Ingredient ingredient : ingredients) {
					JLabel ingredientName = new JLabel(ingredient.getName());
					SpinnerNumberModel minMax = new SpinnerNumberModel(0,0,100000,1);
					JSpinner ingredientQuantity = new JSpinner();
					ingredientQuantity.setModel(minMax);
					
					//Load value from recipe
					if(recipe.containsKey(ingredient)) {
						ingredientQuantity.setValue(recipe.get(ingredient));
					}
					
					addField(ingredientName,ingredientQuantity);
					ingredientMap.put(ingredient, ingredientQuantity);
				}
				
				//Create UI for editing restocking information
				JLabel restockThresholdLabel = new JLabel("Restock Threshold");
				SpinnerNumberModel minMax = new SpinnerNumberModel(0,0,100000,1);
				restockThreshold = new JSpinner();
				restockThreshold.setModel(minMax);
				JLabel restockAmountLabel = new JLabel("Restock Amount");
				restockAmount = new JSpinner();
				restockThreshold.setModel(minMax);
				
				//Get previous values for restocking
				Number restockThresholdValue = server.getRestockThreshold(dish);
				Number restockAmountValue = server.getRestockAmount(dish);
				restockThreshold.setValue(restockThresholdValue);
				restockAmount.setValue(restockAmountValue);
				
				//Add restocking to UI
				JLabel optionsLabel = new JLabel("");
				JSeparator options = new JSeparator();
				addField(optionsLabel,options);
				addField(restockThresholdLabel,restockThreshold);
				addField(restockAmountLabel,restockAmount);
			}
			
			public Number getRestockThreshold() {
				return (Number) restockThreshold.getValue();
			}
			
			public Number getRestockAmount() {
				return (Number) restockAmount.getValue();
			}

			public void addField(JLabel label, JComponent field) {
				groupLabels.addComponent(label);
			    groupFields.addComponent(field);
			    groupRows.addGroup(layout.createParallelGroup()
			        .addComponent(label)
			        .addComponent(field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
			}
			
		}
	}
	
	/**
	 * Window to edit ingredient restocking levels
	 *
	 */
	public class IngredientFrame extends JFrame {

		private static final long serialVersionUID = 3617824143704795063L;
		private ServerInterface server;
		private Ingredient ingredient;

		/**
		 * Create a new ingredient restocking configuration window, linked to the given ingredient and server
		 * @param server backend server to call
		 * @param ingredient ingredient to edit restocking levels for
		 */
		public IngredientFrame(ServerInterface server, Ingredient ingredient) {
			super("Edit ingredient: " + ingredient.getName());
			
			setSize(400,200);
			setLocationRelativeTo(null);
			
			this.server = server;
			this.ingredient = ingredient;
			setLayout(new BorderLayout());
			
			IngredientEditor ingredientEditor = new IngredientEditor();
			JScrollPane scroller = new JScrollPane(ingredientEditor);
			add(scroller,BorderLayout.CENTER);
			
			JPanel buttons = new JPanel(new GridLayout(1,2));
			JButton save = new JButton("Save");
			JButton cancel = new JButton("Cancel");
			buttons.add(cancel);
			
			//Save recipe and restocking levels
			buttons.add(save);
			save.addActionListener(e -> {
				server.setRestockLevels(ingredient, ingredientEditor.getRestockThreshold(), ingredientEditor.getRestockAmount());
				this.dispose();
			});
			cancel.addActionListener(e -> {
				this.dispose();
			});
			add(buttons,BorderLayout.SOUTH);
			
			//Set the save button as the default button
			JRootPane rootPane = SwingUtilities.getRootPane(this);
			rootPane.setDefaultButton(save);
		}
		
		/**
		 * Ingredient editor panel which provides the fields for editing the restocking levels and amounts
		 *
		 */
		public class IngredientEditor extends JPanel {
			
			private static final long serialVersionUID = -534578199178503594L;
			private ParallelGroup groupLabels;
			private ParallelGroup groupFields;
			private SequentialGroup groupRows;
			private GroupLayout layout;
			private JSpinner restockThreshold;
			private JSpinner restockAmount;
			
			/**
			 * Create a new ingredient editor panel with the relevant fields
			 */
			public IngredientEditor() {
				//Setup layout
				layout = new GroupLayout(this);
				setLayout(layout);
				layout.setAutoCreateContainerGaps(true);
				layout.setAutoCreateGaps(true);
		
				groupLabels = layout.createParallelGroup();
				groupFields = layout.createParallelGroup();
				groupRows = layout.createSequentialGroup();
				layout.setHorizontalGroup(layout.createSequentialGroup()
					    .addGroup(groupLabels)
					    .addGroup(groupFields));
				layout.setVerticalGroup(groupRows);
				
				//Create UI for editing restocking information
				JLabel restockThresholdLabel = new JLabel("Restock Threshold");
				SpinnerNumberModel minMax = new SpinnerNumberModel(0,0,100000,1);
				restockThreshold = new JSpinner();
				restockThreshold.setModel(minMax);
				JLabel restockAmountLabel = new JLabel("Restock Amount");
				restockAmount = new JSpinner();
				restockThreshold.setModel(minMax);
				
				//Get previous values for restocking
				Number restockThresholdValue = server.getRestockThreshold(ingredient);
				Number restockAmountValue = server.getRestockAmount(ingredient);
				restockThreshold.setValue(restockThresholdValue);
				restockAmount.setValue(restockAmountValue);
				
				//Add restocking to UI
				addField(restockThresholdLabel,restockThreshold);
				addField(restockAmountLabel,restockAmount);
			}
			
			public Number getRestockThreshold() {
				return (Number) restockThreshold.getValue();
			}
			
			public Number getRestockAmount() {
				return (Number) restockAmount.getValue();
			}

			public void addField(JLabel label, JComponent field) {
				groupLabels.addComponent(label);
			    groupFields.addComponent(field);
			    groupRows.addGroup(layout.createParallelGroup()
			        .addComponent(label)
			        .addComponent(field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
			}
			
		}
	}
	
	public class ConfigurationPanel extends JPanel {
		public ConfigurationPanel() {
			JButton loadConfiguration = new JButton("Load Configuration");
			add(loadConfiguration);
			loadConfiguration.addActionListener(e -> {
				JFileChooser chooser = new JFileChooser();
				int result = chooser.showOpenDialog(this);
				if(result == JFileChooser.APPROVE_OPTION) {
					File configFile = chooser.getSelectedFile();
					try {
						server.loadConfiguration(configFile.getAbsolutePath());
					} catch (FileNotFoundException exception) {
						JOptionPane.showMessageDialog(orderPanel, "Unable to load configuration file: " + exception.getMessage());
					}
				}
			});
		}
	}

	/**
	 * Refresh all parts of the server application based on receiving new data, calling the server afresh
	 */
	public void refreshAll() {
		//Refresh all panels
        ingredientPanel.refresh();
        dishPanel.refresh();
        orderPanel.refresh();
        dronePanel.refresh();
        staffPanel.refresh();
        postcodePanel.refresh();
        supplierPanel.refresh();
        userPanel.refresh();
	}
	
	@Override
	/**
	 * Respond to the model being updated by refreshing all data displays
	 */
	public void updated(UpdateEvent updateEvent) {
		refreshAll();
	}
	
}