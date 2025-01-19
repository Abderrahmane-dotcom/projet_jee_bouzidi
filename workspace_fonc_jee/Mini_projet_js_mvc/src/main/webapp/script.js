// Fonction pour valider le nom de la base de données
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
        url: '/Mini_projet_js_mvc/servlet_controller',
        type: 'GET',
        data: { action: 'validateDatabase', dbName: dbName },
        success: function(response) {
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

// Function to load tables from the validated database
function loadTables(dbName) {
    $.ajax({
        url: '/Mini_projet_js_mvc/servlet_controller',
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

// Function to load columns for the selected table
function loadColumns() {
    const dbName = document.getElementById('databaseName').value;
    const tableName = $('#tables').val();

    if (!tableName) {
        $('#columnsSection').hide();
        return;
    }

    $.ajax({
        url: '/Mini_projet_js_mvc/servlet_controller',
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

// Function to fetch data based on the selected table and columns
function fetchData() {
    const dbName = document.getElementById('databaseName').value;
    const tableName = $('#tables').val();
    const selectedColumns = $('#columns input:checked')
        .map(function () { return this.value; })
        .get();
    const format = $('#formatSelect').val();

    if (!dbName || !tableName || selectedColumns.length === 0) {
        alert("Veuillez sélectionner une base de données, une table et au moins une colonne.");
        return;
    }

    $.ajax({
        url: '/Mini_projet_js_mvc/servlet_controller',
        type: 'GET',
        data: {
            action: 'fetchData',
            dbName: dbName,
            tableName: tableName,
            columns: selectedColumns.join(','),
            format: format
        },
        success: function (response) {
            const fileName = `${dbName}_${tableName}`;

            if (format === 'json') {
                // Convertir les données en JSON formaté
                const json = JSON.stringify(response, null, 2);
                const blob = new Blob([json], { type: 'application/json;charset=utf-8;' });
                const url = URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.download = `${fileName}.json`;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            } else if (format === 'csv') {
                const csv = Papa.unparse(response);
                const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
                const url = URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.download = `${fileName}.csv`;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            } else if (format === 'xls' || format === 'xlsx') {
                const ws = XLSX.utils.json_to_sheet(response);
                const wb = XLSX.utils.book_new();
                XLSX.utils.book_append_sheet(wb, ws, 'Data');
                XLSX.writeFile(wb, `${fileName}.xls`);
            } else if (format === 'pdf') {
                const { jsPDF } = window.jspdf;
                const doc = new jsPDF();
                const rows = response.map(row => Object.values(row));
                const columns = Object.keys(response[0]);

                doc.text("Données Exportées", 10, 10);
                doc.autoTable({ head: [columns], body: rows });
                doc.save(`${fileName}.pdf`);
            } else if (format === 'xml') {
                const x2js = new X2JS();
                const transformedResponse = {};
                response.forEach((item, index) => {
                    transformedResponse[`ligne${index + 1}`] = item;
                });
                let xml = x2js.json2xml_str({ [tableName]: transformedResponse });
                xml = `<?xml version="1.0" encoding="UTF-8"?>\n${xml}`;
                const blob = new Blob([xml], { type: 'application/xml;charset=utf-8;' });
                const url = URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.download = `${fileName}.xml`;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            } else {
                alert("Format non pris en charge !");
            }
        },
        error: function (xhr) {
            console.error(`Erreur: ${xhr.status} - ${xhr.statusText}`);
            alert("Erreur lors de la récupération des données.");
        }
    });
}
