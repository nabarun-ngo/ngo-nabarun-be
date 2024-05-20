(function($) {
	"use strict";




	//join us form validation
	$('#JoinUsForm').validate({ // initialize the plugin
		rules: {
			firstName: {
				required: true,
				rangelength: [2, 20]
			},
			lastName: {
				required: true,
				rangelength: [2, 20]
			},
			email: {
				required: true,
				email: true
			},
			contactNumber: {
				required: true,
				number: true,
				minlength: mobileNoMinLengthValidator,
				maxlength: mobileNoMaxLengthValidator
			},
			hometown: {
				required: true,
				minlength: 5,
				maxlength: 30
			},
			howDoUKnowAboutNabarun: {
				required: true,
				minlength: 5,
				maxlength: 100
			},
			reasonForJoining: {
				required: true,
				minlength: 5,
				maxlength: 100
			},
		},
		submitHandler: function(form) {
			return true;
		},
		errorPlacement: function(error, element) {
			error.insertAfter(element.parent());
		}
	});

	$('#phonecode').change(function(c) {
		$('#mobileno').val('');
	})

	function mobileNoMinLengthValidator() {
		let code = $('#phonecode').val();
		if (code === '91') {
			return 10;
		} else {
			return 8;
		}
	}

	function mobileNoMaxLengthValidator() {
		let code = $('#phonecode').val();
		if (code === '91') {
			return 10;
		} else {
			return 13;
		}
	}





	function getFormData($form) {
		var unindexed_array = $form.serializeArray();
		var indexed_array = {};
		$.map(unindexed_array, function(n, i) {
			indexed_array[n['name']] = n['value'];
		});
		return indexed_array;
	}
	
	
	

$('#passwordForm').validate({ // initialize the plugin
		rules: {
			
			email: {
				required: true,
				email: true
			},
			password: {
				required: true,
				minlength: 6,
				maxlength: 30
			},
			confirmPassword: {
				required: true,
				equalTo: "#password"
			}
		},
		submitHandler: function(form) {
			return true;
		},
		errorPlacement: function(error, element) {
			error.insertAfter(element.parent());
		}
	});
	


})(jQuery); 