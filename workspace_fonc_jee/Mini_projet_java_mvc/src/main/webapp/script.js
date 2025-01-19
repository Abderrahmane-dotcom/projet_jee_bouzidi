// Fonction pour valider ou non l'existance de la base de données
function validateDatabase() {
    const dbName = document.getElementById('databaseName').value;

	const columnsDiv = $('#columns');
	columnsDiv.empty();
	$('#columnsSection').hide();
    if (!dbName) {
        alert("Veuillez entrer le nom de la base de données.");
        return;
    }

    $.ajax({
        url: '/Mini_projet_java_mvc/servlet_controller',
        type: 'GET',
        data: { action: 'validateDatabase', dbName: dbName },
		success: function(response) {
		    if (response.error) {
		        alert(response.error); // Affiche l'erreur si elle existe dans la réponse
		        return;
		    }

		    if (response.success) {
		        alert(`Base de données ${response.dbName} validée.`);
		        loadTables(dbName);
		        $('#tablesSection').show();
		    }
		},
        error: function(xhr) {
            if (xhr.status === 404) {
                alert("Base de données non trouvée.");
            } else {
                alert("Erreur lors de la validation de la base de données.");
            }
        }
    });
}

// Fonction pour charger les tables du schéma choisi
function loadTables(dbName) {
    $.ajax({
        url: '/Mini_projet_java_mvc/servlet_controller',
        type: 'GET',
        data: { action: 'loadTables', dbName: dbName },
        success: function(response) {
            const tablesDropdown = $('#tables');
            tablesDropdown.empty();
            tablesDropdown.append('<option value="">-- Sélectionnez une table --</option>');
            response.forEach(table => {
                tablesDropdown.append(`<option value="${table}">${table}</option>`);
            });
        },
        error: function() {
            alert("Erreur lors de la récupération des tables.");
        }
    });
}

// Fonction pour charger les colonnes de la table choisie
function loadColumns() {
    const dbName = document.getElementById('databaseName').value;
    const tableName = $('#tables').val();

    if (!tableName) {
        $('#columnsSection').hide();
        return;
    }

    $.ajax({
        url: '/Mini_projet_java_mvc/servlet_controller',
        type: 'GET',
        data: { action: 'loadColumns', dbName: dbName, tableName: tableName },
        success: function(response) {
            const columnsDiv = $('#columns');
            columnsDiv.empty();
            response.forEach(column => {
                const columnHTML = `
                    <div>
                        <input type="checkbox" id="${column}" value="${column}">
                        <label for="${column}">${column}</label>
                    </div>`;
                columnsDiv.append(columnHTML);
            });
            $('#columnsSection').show();
        },
        error: function() {
            alert("Erreur lors de la récupération des colonnes.");
        }
    });
}
//fonction pour envoyer la requête pour réupérer le fichier résultat 
function fetchData() {
    const dbName = document.getElementById('databaseName').value;
    const tableName = $('#tables').val();
    const selectedColumns = $('#columns input:checked')
        .map(function() { return this.value; })
        .get();
    const format = $('#formatSelect').val();
    const action = "fetchData";  // Correction de l'action à "fetchData"

    // Vérification des paramètres nécessaires
    if (!dbName || !tableName || selectedColumns.length === 0 || !format) {
        alert("Veuillez sélectionner une base de données, une table, au moins une colonne, et un format.");
        return;
    }

    // Création de l'URL avec les paramètres
    const url = `/Mini_projet_java_mvc/servlet_controller?dbName=${encodeURIComponent(dbName)}&tableName=${encodeURIComponent(tableName)}&columns=${encodeURIComponent(selectedColumns.join(','))}&format=${encodeURIComponent(format)}&action=${encodeURIComponent(action)}`;

    // Utilisation de fetch pour envoyer la requête
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error("Erreur lors de la récupération des données : " + response.statusText);
            }
            return response.blob(); // Utilise blob pour les fichiers binaires (PDF, XLS)
        })
        .then(blob => {
            // Déterminer le nom du fichier et l'extension selon le format
            let fileName = dbName + "_" + tableName + "_data";
            let fileType = 'application/octet-stream'; // par défaut pour les fichiers binaires

            switch (format) {
                case 'json':
                    fileName += '.json';
                    fileType = 'application/json';
                    break;
                case 'xml':
                    fileName += '.xml';
                    fileType = 'application/xml';
                    break;
                case 'csv':
                    fileName += '.csv';
                    fileType = 'text/csv';
                    break;
                case 'xls':
                    fileName += '.xls';
                    fileType = 'application/vnd.ms-excel';
                    break;
                case 'pdf':
                    fileName += '.pdf';
                    fileType = 'application/pdf';
                    break;
                default:
                    fileName += '.txt';
                    fileType = 'text/plain';
                    break;
            }

            // Créer un lien pour télécharger le fichier
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = fileName;
            link.click();
        })
        .catch(error => {
            alert("Erreur : " + error.message);
        });
}
