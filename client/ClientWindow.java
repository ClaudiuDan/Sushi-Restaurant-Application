package client;

import java.awt.*;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.*;
import javax.swing.GroupLayout.*;
import javax.swing.table.AbstractTableModel;

import common.*;

/**
 * Displays the Sushi Client user interface
 * You should not need to modify this class if your implementation of the interface is correct.
 *
 */
public class ClientWindow extends JFrame implements UpdateListener {

	private static final long serialVersionUID = -3193345755801430084L;
	private ClientInterface client;
	private User user;
	private JLabel loggedIn;
	private MenuPanel menuPanel;
	private OrderPanel orderPanel;
	private JTabbedPane tabs;
	
	private static final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance();

	/**
	 * Creates the Sushi Client user interface, following a successful login
	 * @param client
	 */
	public ClientWindow(ClientInterface client) {
		super("Sushi Client");
		this.client = client;
		
		//Listen for updates from the client backend and update the UI appropriately
		client.addUpdateListener(this);
		
		//Launch the login window on startup, before displaying client UI 
		LoginWindow loginWindow = new LoginWindow();
		loginWindow.setSuccess(user -> { 
			initialiseWindow(user);	
		});
		
	}

	private void initialiseWindow(User user) {		
		//Display window
		setSize(800,600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		//Start timed updates
		startTimer();
			
		//Prepare the client layout
		setLayout(new BorderLayout());
		
		//Top part of the UI displays the currently logged in user
		JPanel top = new JPanel();
		loggedIn = new JLabel();
		top.add(loggedIn);
		
		//Set user
		setUser(user);
		
		//Have 2 tabs to show the menu and current orders
		JPanel middle = new JPanel(new BorderLayout());
		tabs = new JTabbedPane();
		middle.add(tabs,BorderLayout.CENTER);
		menuPanel = new MenuPanel();
		orderPanel = new OrderPanel();
		tabs.addTab("Menu", menuPanel);
		tabs.addTab("Orders", orderPanel);	
		
		add(top,BorderLayout.NORTH);
		add(middle,BorderLayout.CENTER);
	}

	@Override
	public void updated(UpdateEvent updateEvent) {
		//When the client backend updates, update the basket, dishes and current orders
		menuPanel.refreshBasket();
		menuPanel.refreshDishes();
		orderPanel.refreshOrders();
	}
	
	/**
	 * Set the logged in user to the given user model
	 * @param user
	 */
	private void setUser(User user) {
		this.user = user;
		loggedIn.setText("Logged in as " + user.getName());
	}

	/**
	 * Display a menu panel with a menu on the left side and the basket on the right side. 
	 * Allow adding items from the menu to the basket and checking out into an order.
	 *
	 */
	public class MenuPanel extends JPanel {
		
		HashMap<Dish,JPanel> menuItems = new HashMap<Dish,JPanel>(); //Holds a mapping of the dish to the menu panel for the menu
		private JPanel menu;
		private GridBagConstraints d;
		
		private JPanel basket;
		HashMap<Dish,JSpinner> basketMap = new HashMap<Dish,JSpinner>(); //Holds a mapping of dish to quantity spinner for the basket
		private JPanel basketInner;
		private JLabel basketCost;
		private JButton checkoutBasket;
		
		/**
		 * Creates a new menu panel, with inner menu and basket panels
		 */
		public MenuPanel() {			
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.NORTH;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.weighty = 1;
			setLayout(new GridLayout(1,2));
			
			//Menu
			menu = new JPanel();
			menu.setBackground(new Color(255,255,255));
			
			GridBagLayout dishLayout = new GridBagLayout();
			menu.setLayout(dishLayout);
			d = new GridBagConstraints();
			d.weighty = 1;
			d.weightx = 1;
			d.anchor = GridBagConstraints.FIRST_LINE_START;
			d.fill = GridBagConstraints.HORIZONTAL;
			d.insets = new Insets(3,3,3,3);
			
			JScrollPane menuScroller = new JScrollPane(menu);
			add(menuScroller);
			
			JLabel menuLabel = new JLabel("Menu");
			menuLabel.setHorizontalAlignment(SwingConstants.CENTER);
			menuLabel.setFont(new Font(menuLabel.getFont().getName(),Font.BOLD,32));
			menu.add(menuLabel,d);
			d.gridy++;
			
			//Basket
			basket = new JPanel();
			basket.setLayout(new BorderLayout());
			basket.setBackground(new Color(200,200,200));
			
			JLabel basketLabel = new JLabel("Basket");
			basketLabel.setFont(new Font(basketLabel.getFont().getName(),Font.BOLD,32));
			basketLabel.setHorizontalAlignment(SwingConstants.CENTER);
			basket.add(basketLabel,BorderLayout.NORTH);
			
			JPanel basketButtons = new JPanel(new BorderLayout());
			JButton updateBasket = new JButton("Update");
			updateBasket.addActionListener(e -> {
				updateBasket();
			});
			basketButtons.add(updateBasket,BorderLayout.WEST);
			
			basketCost = new JLabel("No basket");
			basketCost.setHorizontalAlignment(SwingConstants.CENTER);
			basketButtons.add(basketCost,BorderLayout.CENTER);
			
			//Checkout button
			checkoutBasket = new JButton("Checkout");
			checkoutBasket.setEnabled(false);
			checkoutBasket.addActionListener(e -> {
				checkoutBasket();
			});
			basketButtons.add(checkoutBasket,BorderLayout.EAST);
			
			basketInner = new JPanel();
			basketInner.setLayout(new BoxLayout(basketInner,BoxLayout.Y_AXIS));

			basket.add(basketButtons,BorderLayout.SOUTH);
			
			JScrollPane basketScroller = new JScrollPane(basketInner);
			basket.add(basketScroller,BorderLayout.CENTER);
			add(basket);
			
			refreshDishes();
		}

		/**
		 * Check out the basket into a new order
		 */
		private void checkoutBasket() {
			client.checkoutBasket(user);
			tabs.setSelectedIndex(1);
			
			refreshBasket();
			orderPanel.refreshOrders();
		}

		/** 
		 * Add a new dish to the menu display
		 * @param dish The dish to display
		 */
		private void addDish(Dish dish) {
			d.gridy++;
			JPanel dishPanel = new JPanel();
			dishPanel.setLayout(new BorderLayout());
			dishPanel.setBorder(BorderFactory.createLineBorder(Color.black));
			JLabel dishLabel = new JLabel("<html><strong>" + dish.getName() + " (" + moneyFormat.format(client.getDishPrice(dish)) + ")</strong><p>" + client.getDishDescription(dish) + "</p></html>");
			dishLabel.setHorizontalAlignment(SwingConstants.LEFT);
			
			SpinnerNumberModel minMax = new SpinnerNumberModel(0,0,100000,1);
			JSpinner quantity = new JSpinner();
			quantity.setModel(minMax);
			
			JButton addToBasket = new JButton("Add to Basket");
			
			dishPanel.add(dishLabel,BorderLayout.NORTH);
			
			JPanel buttonPanel = new JPanel(new BorderLayout());
			buttonPanel.add(quantity,BorderLayout.WEST);
			buttonPanel.add(addToBasket,BorderLayout.EAST);
			dishPanel.add(buttonPanel);
			
			addToBasket.addActionListener(e -> {
				client.addDishToBasket(user, dish, (Number) quantity.getValue()); 
				menuPanel.refreshBasket();
				quantity.setValue(0);
			});
			
			menu.add(dishPanel,d);
			
			menuItems.put(dish, dishPanel);
		}
		
		/**
		 * Refresh the current items of the basket
		 */
		private void refreshBasket() {
			basketInner.removeAll();
			basketMap.clear();
			
			//Get all current items in the basket from the backend
			Map<Dish, Number> basketItems = client.getBasket(user);
			for (Entry<Dish, Number> basketItem : basketItems.entrySet()) {
				Dish basketDish = basketItem.getKey();
				Number basketQuantity = basketItem.getValue();
				JPanel basketPanel = new JPanel(new BorderLayout());
				basketPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));
				SpinnerNumberModel minMax = new SpinnerNumberModel(0,0,100000,1);
				JSpinner basketQuantitySpinner = new JSpinner();
				basketQuantitySpinner.setModel(minMax);
				basketQuantitySpinner.setValue(basketQuantity);
				basketPanel.add(basketQuantitySpinner,BorderLayout.WEST);

				JLabel basketLabel = new JLabel(basketDish.getName());
				basketPanel.add(basketLabel,BorderLayout.CENTER);
				basketInner.add(basketPanel);
				basketMap.put(basketDish, basketQuantitySpinner);
			}
			
			//Get the total cost of the basket
			Number cost = client.getBasketCost(user);
			basketCost.setText(moneyFormat.format(cost));
			
			//If there's at least 1 item, enable checkout
			if(basketItems.size() > 0) {
				checkoutBasket.setEnabled(true);
			} else {
				checkoutBasket.setEnabled(false);
			}
			
			//Redraw the basket
			basketInner.revalidate();
			basketInner.repaint();
		}
		
		/**
		 * Remove a dish from the menu
		 * @param dish Dish to remove from the menu, typically removed from the backend
		 */
		private void removeDish(Dish dish) {
			JPanel previous = menuItems.get(dish);
			menu.remove(previous);
		}
		
		/**
		 * Refresh the menu dishes from the backend. Remove any outdated items and add new items.
		 */
		public void refreshDishes() {
			//Compare old and new dishes
			List<Dish> dishes = client.getDishes();
			Set<Dish> oldDishes = menuItems.keySet();
			
			//Look for new
			for(Dish dish : dishes) {
				if(!oldDishes.contains(dish)) {
					addDish(dish);
				}
			}
			
			//Look for removed
			for(Dish dish : oldDishes) {
				if(!dishes.contains(dish)) {
					removeDish(dish);
				}
			}
		}
		
		/**
		 * Update the basket by synchronising the basket on the backend with the GUI
		 */
		private void updateBasket() {
			//Identify items in the basket that need updating
			HashMap<Dish,Number> modifications = new HashMap<Dish,Number>();
			for(Entry<Dish, JSpinner> basketMapItem : basketMap.entrySet()) {
				Dish basketDish = basketMapItem.getKey();
				JSpinner basketSpinner = basketMapItem.getValue();
				Number basketQuantity = (Number) basketSpinner.getValue();
				modifications.put(basketDish, basketQuantity);
			}
			
			//Perform update
			for(Entry<Dish,Number> modification : modifications.entrySet()) {
				client.updateDishInBasket(user, modification.getKey(), modification.getValue());
			}
			
			refreshBasket();
		}


	}
	
	/**
	 * Displays a list of the current orders and their statuses
	 *
	 */
	public class OrderPanel extends JPanel {
		
		ResultsTable<Order> ordersTable;
		private JTable table;
		
		/**
		 * Create an order panel with a table listing of current orders
		 */
		public OrderPanel() {
			setLayout(new BorderLayout());
			
			//Create the data table to display orders
			table = new JTable();
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			ordersTable = new ResultsTable<Order>(new String[] {"Name"}, () -> client.getOrders(user));
			HashMap<String, Supplier<Map<? extends Model, Object>>> extras = new HashMap<String, Supplier<Map<? extends Model, Object>>>();
			HashMap<String, Function<Model, Object>> extras2 = new HashMap<String, Function<Model, Object>>();
			extras2.put("Cost", order -> client.getOrderCost((Order) order));
			extras2.put("Status", order -> client.getOrderStatus((Order) order));
			ordersTable.setColumns(new String[] {"Name", "Status", "Cost"},extras,extras2);
			table.setModel(ordersTable);
			
			//Make table scrollable
			JScrollPane pane = new JScrollPane(table);
			add(pane,BorderLayout.CENTER);
			
			//Add a button to cancel an order
			JPanel buttons = new JPanel(new BorderLayout());
			JButton cancelOrder = new JButton("Cancel Order");
			cancelOrder.addActionListener(e -> {
				int index = table.getSelectedRow();
				if(index < 0) return;
				Order order = (Order) ordersTable.getValue(index);
				client.cancelOrder(order);
			});
			
			buttons.add(cancelOrder,BorderLayout.WEST);
			add(buttons,BorderLayout.SOUTH);			
		}
		
		/**
		 * Refresh all the orders being displayed in the table by fetching from the client
		 */
		public void refreshOrders() {
			//Store previous selection
			int previous = table.getSelectedRow();
			
			//Update the model
			orderPanel.ordersTable.refresh();	
			
			//Reselect
			if(previous >= 0 && previous < table.getRowCount()) {
				table.setRowSelectionInterval(previous, previous);
			}
			
		}
	}
	
	/**
	 * Provides a login and registration window with a username and password
	 *
	 */
	public class LoginWindow extends JFrame {
		private static final long serialVersionUID = -4632043171851342432L;

		//Set up the layout manager to display labels on the left and fields on the right
		private ParallelGroup groupLabels;
		private ParallelGroup groupFields;
		private SequentialGroup groupRows;
		private GroupLayout layout;

		//Function to be called when login is successful
		private Consumer<User> success;

		/** Create a Login Window with login and registration panels */
		public LoginWindow() {
			LoginPanel loginPanel = new LoginPanel();
			RegisterPanel registerPanel = new RegisterPanel();

			//Set up tabs
			JTabbedPane tabs = new JTabbedPane();
			tabs.addTab("Login",loginPanel);
			tabs.addTab("Register",registerPanel);
			add(tabs);

			setSize(400,250);
			setLocationRelativeTo(null);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setVisible(true);
			
			//If a user is returned, log them in. Otherwise, display an error
			loginPanel.login.addActionListener(e -> {
				String u = loginPanel.username.getText();
				String p = String.valueOf(loginPanel.password.getPassword());
				User user = client.login(u, p);
				if(user != null) {
					this.dispose();
					success.accept(user);
				} else {
					JOptionPane.showMessageDialog(this, "Invalid username or password");
				}
			});
			
			//If a user is returned, log them in (and register). Otherwise, display an error
			registerPanel.register.addActionListener(e -> {
				String u = registerPanel.username.getText();
				String p = String.valueOf(registerPanel.password.getPassword());
				Postcode code = (Postcode) registerPanel.location.getSelectedItem();
				String address = registerPanel.address.getText();
				User user = client.register(u,p,address,code);
				if(user != null) {
					this.dispose();
					success.accept(user);
				} else {
					JOptionPane.showMessageDialog(this, "Unable to register account. Please ensure you've filled in all required details");
				}
			});
		}

		/**
		 * Set the function to be called when login is successful
		 * @param success Successful function to be called on completion
		 */
		public void setSuccess(java.util.function.Consumer<User> success) {
			this.success = success;
		}

		/**
		 * Login panel, which takes a username and password
		 *
		 */
		public class LoginPanel extends JPanel {

			private static final long serialVersionUID = 6908873031511662190L;
			private JButton login;
			private JTextField username;
			private JPasswordField password;

			/**
			 * Create a login panel with username and password fields
			 */
			public LoginPanel() {				
				setLayout(new BorderLayout());
				JPanel top = new JPanel();
				add(top,BorderLayout.CENTER);
				layout = new GroupLayout(top);
				layout.setAutoCreateContainerGaps(true);
				layout.setAutoCreateGaps(true);
				top.setLayout(layout);
				
				groupLabels = layout.createParallelGroup();
				groupFields = layout.createParallelGroup();
				groupRows = layout.createSequentialGroup();
				layout.setHorizontalGroup(layout.createSequentialGroup()
						.addGroup(groupLabels)
						.addGroup(groupFields));
				layout.setVerticalGroup(groupRows);
				
				JLabel usernameLabel = new JLabel("Username");
				username = new JTextField();
				
				JLabel passwordLabel = new JLabel("Password");
				password = new JPasswordField();
				
				addField(usernameLabel,username);
				addField(passwordLabel,password);
				
				JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				add(bottom,BorderLayout.SOUTH);
				login = new JButton("Login");
				bottom.add(login);
			}

			/**
			 * Add a label and field to the panel
			 * @param label JLabel with caption
			 * @param field JComponent field to go with the caption
			 */
			public void addField(JLabel label, JComponent field) {
				groupLabels.addComponent(label);
				groupFields.addComponent(field);
				groupRows.addGroup(layout.createParallelGroup()
						.addComponent(label)
						.addComponent(field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
			}
		}

		/**
		 * Registration panel, which takes a username, password and postcode
		 *
		 */
		public class RegisterPanel extends JPanel {

			private static final long serialVersionUID = 6908873031511662190L;
			private ParallelGroup groupLabels;
			private ParallelGroup groupFields;
			private SequentialGroup groupRows;
			private GroupLayout layout;
			private JComboBox<Postcode> location;
			private JPasswordField password;
			private JTextField username;
			private JTextArea address;
			private JButton register;

			/**
			 * Create registration panel with username, password and postcode
			 */
			public RegisterPanel() {
				setLayout(new BorderLayout());
				JPanel top = new JPanel();
				add(top,BorderLayout.CENTER);
				layout = new GroupLayout(top);
				layout.setAutoCreateContainerGaps(true);
				layout.setAutoCreateGaps(true);
				top.setLayout(layout);
				
				groupLabels = layout.createParallelGroup();
				groupFields = layout.createParallelGroup();
				groupRows = layout.createSequentialGroup();
				layout.setHorizontalGroup(layout.createSequentialGroup()
						.addGroup(groupLabels)
						.addGroup(groupFields));
				layout.setVerticalGroup(groupRows);
				
				//Create fields
				JLabel usernameLabel = new JLabel("Username");
				username = new JTextField();
				
				JLabel passwordLabel = new JLabel("Password");
				password = new JPasswordField();
				
				JLabel addressLabel = new JLabel("Address");
				address = new JTextArea();
				
				JLabel locationLabel = new JLabel("Locations");
				location = new JComboBox<Postcode>();
				location.setModel(new ComboModel<Postcode>(() -> client.getPostcodes()));
				if(location.getModel().getSize() > 0) {
					location.setSelectedIndex(0);
				}
			
				//Add fields to UI
				addField(usernameLabel,username);
				addField(passwordLabel,password);
				addField(addressLabel,address);
				addField(locationLabel,location);
				
				JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				add(bottom,BorderLayout.SOUTH);
				register = new JButton("Register");
				bottom.add(register);
			}

			/**
			 * Create a combobox model driven off of the backend data source
			 *
			 * @param <T> Model
			 */
			public class ComboModel<T> extends AbstractListModel<T> implements ComboBoxModel<T> {

				private static final long serialVersionUID = 3885818221611239892L;
				private Object selected;
				private java.util.function.Supplier<List<? extends Model>> dataSource;
				
				/**
				 * Create a new combobox model driven off the provided data source supplier
				 * @param dataSource
				 */
				public ComboModel(java.util.function.Supplier<List<? extends Model>> dataSource) {
					this.dataSource = dataSource;
				}

				@Override
				public int getSize() {
					return dataSource.get().size();
				}

				@Override
				public T getElementAt(int index) {
					return (T) dataSource.get().get(index);
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
	}


	/**
	 * Start the background timer to update on a regular interval
	 */
	public void startTimer() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);     
		int timeInterval = 5;
		
		//Refresh the status of orders every 5 seconds
        scheduler.scheduleAtFixedRate(() -> refreshOrders(), 0, timeInterval, TimeUnit.SECONDS);
	}
	
	/**
	 * Refresh the order display
	 */
	public void refreshOrders() {
		if(orderPanel != null) {
			orderPanel.refreshOrders();
		}
	}
	
	/**
	 * Provide a datasource driven model for a JTable
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
		 * Create a new results table
		 * @param columns list of column names
		 * @param dataSource data source to drive the results
		 */
		public ResultsTable(String[] columns, java.util.function.Supplier<List<? extends Model>> dataSource) {
			this.columns = columns;
			this.dataSource = dataSource;
			
			list = dataSource.get();
			refresh();
		}

		/** 
		 * Set the columns to be displayed, including additional closure-based columns
		 * @param columns Standard columns
		 * @param extras A supplier which provides a full list of models to values
		 * @param extras2 A function which takes a model and returns a value
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
		 * Refresh the table based on all the given columns and additional values
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

}