// API Endpoints for the Java backend
const API_BASE_URL = "http://localhost:8080/api";

// DOM Elements
document.addEventListener('DOMContentLoaded', function() {
    // Navigation elements
    const navLinks = document.querySelectorAll('nav a');
    const sections = document.querySelectorAll('section');

    // Modal elements
    const editModal = document.getElementById('edit-modal');
    const deleteModal = document.getElementById('delete-modal');
    const closeBtn = document.querySelector('.close');
    const cancelEditBtn = document.getElementById('cancel-edit');
    const cancelDeleteBtn = document.getElementById('cancel-delete');
    const confirmDeleteBtn = document.getElementById('confirm-delete');

    // Form elements
    const addBookForm = document.getElementById('add-book-form');
    const editBookForm = document.getElementById('edit-book-form');
    const searchButton = document.getElementById('search-button');

    // Results containers
    const booksListContainer = document.getElementById('books-list');
    const searchResultsContainer = document.getElementById('search-results');
    
    // Empty state elements
    const emptyCollection = document.getElementById('empty-collection');
    const noResults = document.getElementById('no-results');
    const addFirstBook = document.getElementById('add-first-book');
    
    // Messages and errors
    const addMessage = document.getElementById('add-message');
    const editMessage = document.getElementById('edit-message');
    const booksError = document.getElementById('books-error');
    const searchError = document.getElementById('search-error');

    // Loading indicators
    const booksLoading = document.getElementById('books-loading');
    const searchLoading = document.getElementById('search-loading');

    // Initialize the application
    init();

    function init() {
        // Set up event listeners
        setupEventListeners();
        
        // Load all books by default
        loadAllBooks();
    }

    function setupEventListeners() {
        // Navigation
        navLinks.forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                
                // Remove active class from all links and sections
                navLinks.forEach(l => l.classList.remove('active'));
                sections.forEach(s => s.classList.remove('active-section'));
                
                // Add active class to clicked link
                this.classList.add('active');
                
                // Show corresponding section
                const targetSection = document.getElementById(this.getAttribute('data-section'));
                targetSection.classList.add('active-section');
                
                // If we're showing the books section, refresh the books
                if (this.getAttribute('data-section') === 'books-section') {
                    loadAllBooks();
                }
                
                // If we're showing the search section, focus the search field
                if (this.getAttribute('data-section') === 'search-section') {
                    document.getElementById('search-input').focus();
                }
            });
        });

        // Empty collection "Add Book" button
        if (addFirstBook) {
            addFirstBook.addEventListener('click', () => {
                // Switch to the add book section
                navLinks.forEach(l => l.classList.remove('active'));
                sections.forEach(s => s.classList.remove('active-section'));
                
                // Activate the Add Book tab
                document.querySelector('[data-section="add-section"]').classList.add('active');
                document.getElementById('add-section').classList.add('active-section');
                
                // Focus the book name field
                document.getElementById('book-name').focus();
            });
        }

        // Add book form
        addBookForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addBook();
        });

        // Edit book form
        editBookForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateBook();
        });

        // Search button
        searchButton.addEventListener('click', function() {
            searchBooks();
        });

        // Enter key in search input
        document.getElementById('search-input').addEventListener('keyup', function(e) {
            if (e.key === 'Enter') {
                searchBooks();
            }
        });

        // Close modal buttons
        closeBtn.addEventListener('click', () => closeModal(editModal));
        cancelEditBtn.addEventListener('click', () => closeModal(editModal));
        
        // Cancel delete
        cancelDeleteBtn.addEventListener('click', () => closeModal(deleteModal));
        
        // Confirm delete
        confirmDeleteBtn.addEventListener('click', deleteBook);
        
        // Close modals when clicking outside
        window.addEventListener('click', (e) => {
            if (e.target === editModal) closeModal(editModal);
            if (e.target === deleteModal) closeModal(deleteModal);
        });
        
        // Escape key closes active modal
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                if (editModal.classList.contains('active')) closeModal(editModal);
                if (deleteModal.classList.contains('active')) closeModal(deleteModal);
            }
        });
    }

    // API Functions
    function loadAllBooks() {
        setLoading(booksLoading, true);
        clearError(booksError);
        
        fetch(`${API_BASE_URL}/books`)
            .then(handleResponse)
            .then(books => {
                renderBooks(books, booksListContainer);
                setLoading(booksLoading, false);
                
                // Show empty state if no books
                if (books.length === 0) {
                    booksListContainer.innerHTML = '';
                    emptyCollection.classList.remove('hidden');
                } else {
                    emptyCollection.classList.add('hidden');
                }
            })
            .catch(error => {
                showError(booksError, "Failed to load books. " + error.message);
                setLoading(booksLoading, false);
                emptyCollection.classList.add('hidden');
            });
    }

    function searchBooks() {
        const query = document.getElementById('search-input').value.trim();
        if (!query) {
            showError(searchError, "Please enter a search term");
            return;
        }
        
        setLoading(searchLoading, true);
        clearError(searchError);
        noResults.classList.add('hidden');
        
        fetch(`${API_BASE_URL}/books?query=${encodeURIComponent(query)}`)
            .then(handleResponse)
            .then(books => {
                renderBooks(books, searchResultsContainer);
                setLoading(searchLoading, false);
                
                // Show empty state for no results
                if (books.length === 0) {
                    searchResultsContainer.innerHTML = '';
                    noResults.classList.remove('hidden');
                } else {
                    noResults.classList.add('hidden');
                }
            })
            .catch(error => {
                showError(searchError, "Search failed. " + error.message);
                setLoading(searchLoading, false);
                noResults.classList.add('hidden');
            });
    }

    function addBook() {
        const name = document.getElementById('book-name').value.trim();
        const author = document.getElementById('book-author').value.trim();
        
        if (!name || !author) {
            showMessage(addMessage, "Please fill in all fields", "error");
            return;
        }
        
        const bookData = { name, author };
        
        // Show loading state
        const submitBtn = addBookForm.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Adding...';
        
        fetch(`${API_BASE_URL}/books`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(bookData)
        })
        .then(handleResponse)
        .then(data => {
            showMessage(addMessage, "Book added to your collection!", "success");
            addBookForm.reset();
            
            // Reset button
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
            
            // Refresh books list (if we were on that tab)
            if (document.getElementById('books-section').classList.contains('active-section')) {
                loadAllBooks();
            }
            
            // Focus the book name field for the next entry
            document.getElementById('book-name').focus();
        })
        .catch(error => {
            showMessage(addMessage, "Failed to add book: " + error.message, "error");
            
            // Reset button
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        });
    }

    function openEditModal(book) {
        // Populate form with book details
        document.getElementById('edit-book-id').value = book.id;
        document.getElementById('edit-book-name').value = book.name;
        document.getElementById('edit-book-author').value = book.author;
        
        // Clear previous messages
        editMessage.textContent = "";
        editMessage.className = "message";
        
        // Show modal with animation
        editModal.classList.add('active');
        
        // Focus the first field
        document.getElementById('edit-book-name').focus();
    }

    function openDeleteModal(book) {
        // Set book id for delete operation
        confirmDeleteBtn.setAttribute('data-id', book.id);
        
        // Show book details
        document.getElementById('delete-book-details').innerHTML = `
            <p><strong>Title:</strong> ${book.name}</p>
            <p><strong>Author:</strong> ${book.author}</p>
        `;
        
        // Show modal with animation
        deleteModal.classList.add('active');
    }

    function updateBook() {
        const id = document.getElementById('edit-book-id').value;
        const name = document.getElementById('edit-book-name').value.trim();
        const author = document.getElementById('edit-book-author').value.trim();
        
        if (!name || !author) {
            showMessage(editMessage, "Please fill in all fields", "error");
            return;
        }
        
        const bookData = { name, author };
        
        // Show loading state
        const submitBtn = editBookForm.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
        
        fetch(`${API_BASE_URL}/books/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(bookData)
        })
        .then(handleResponse)
        .then(data => {
            showMessage(editMessage, "Book updated successfully!", "success");
            
            // Reset button
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
            
            // Refresh books list
            loadAllBooks();
            
            // Close modal after a short delay
            setTimeout(() => {
                closeModal(editModal);
            }, 1200);
        })
        .catch(error => {
            showMessage(editMessage, "Failed to update book: " + error.message, "error");
            
            // Reset button
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        });
    }

    function deleteBook() {
        const id = confirmDeleteBtn.getAttribute('data-id');
        
        // Show loading state
        const originalText = confirmDeleteBtn.innerHTML;
        confirmDeleteBtn.disabled = true;
        confirmDeleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
        
        fetch(`${API_BASE_URL}/books/${id}`, {
            method: 'DELETE'
        })
        .then(handleResponse)
        .then(data => {
            // Close modal
            closeModal(deleteModal);
            
            // Reset button (even though modal is closed)
            confirmDeleteBtn.disabled = false;
            confirmDeleteBtn.innerHTML = originalText;
            
            // Show success message briefly with a toast
            const toast = document.createElement('div');
            toast.className = 'message success';
            toast.style.position = 'fixed';
            toast.style.bottom = '20px';
            toast.style.right = '20px';
            toast.style.padding = '10px 20px';
            toast.style.borderRadius = '4px';
            toast.style.boxShadow = '0 3px 10px rgba(0,0,0,0.2)';
            toast.style.zIndex = '9999';
            toast.textContent = 'Book removed from collection';
            document.body.appendChild(toast);
            
            // Remove toast after 3 seconds
            setTimeout(() => {
                toast.style.opacity = '0';
                setTimeout(() => document.body.removeChild(toast), 300);
            }, 3000);
            
            // Refresh books list
            loadAllBooks();
            
            // Also refresh search results if applicable
            if (document.getElementById('search-section').classList.contains('active-section')) {
                searchBooks();
            }
        })
        .catch(error => {
            // Show error message
            showMessage(document.createElement('div'), "Failed to delete book: " + error.message, "error");
            closeModal(deleteModal);
            
            // Reset button
            confirmDeleteBtn.disabled = false;
            confirmDeleteBtn.innerHTML = originalText;
        });
    }

    // Helper function to handle API responses
    function handleResponse(response) {
        if (!response.ok) {
            // Try to parse error message from response
            return response.json()
                .then(data => {
                    throw new Error(data.message || `HTTP error! Status: ${response.status}`);
                })
                .catch(e => {
                    // If we can't parse JSON, use status text
                    throw new Error(`HTTP error! Status: ${response.status} ${response.statusText}`);
                });
        }
        return response.json();
    }

    // Helper functions
    function renderBooks(books, container) {
        container.innerHTML = '';
        
        if (!books || books.length === 0) {
            // We handle empty states separately now
            return;
        }
        
        books.forEach(book => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${book.id}</td>
                <td>${book.name}</td>
                <td>${book.author}</td>
                <td class="action-buttons">
                    <button class="btn edit-btn" title="Edit book details">
                        <i class="fas fa-edit"></i> Edit
                    </button>
                    <button class="btn delete-btn" title="Remove from collection">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </td>
            `;
            
            // Add event listeners to buttons
            const editBtn = row.querySelector('.edit-btn');
            const deleteBtn = row.querySelector('.delete-btn');
            
            editBtn.addEventListener('click', () => openEditModal(book));
            deleteBtn.addEventListener('click', () => openDeleteModal(book));
            
            // Add hover effect and animation
            row.style.opacity = '0';
            row.style.transform = 'translateY(10px)';
            container.appendChild(row);
            
            // Trigger layout and then animate in
            setTimeout(() => {
                row.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
                row.style.opacity = '1';
                row.style.transform = 'translateY(0)';
            }, 10 + (books.indexOf(book) * 30)); // stagger the animations
        });
    }

    function closeModal(modal) {
        modal.classList.remove('active');
    }

    function showMessage(element, message, type) {
        element.textContent = message;
        element.className = `message ${type}`;
        
        // Scroll to message if it's not in view
        element.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        
        // Clear message after 3 seconds
        setTimeout(() => {
            element.textContent = "";
            element.className = "message";
        }, 3000);
    }

    function showError(element, message) {
        element.textContent = message;
        element.classList.add('active');
        
        // Scroll to error if it's not in view
        element.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function clearError(element) {
        element.textContent = "";
        element.classList.remove('active');
    }

    function setLoading(element, isLoading) {
        if (isLoading) {
            element.classList.remove('hidden');
        } else {
            element.classList.add('hidden');
        }
    }
});